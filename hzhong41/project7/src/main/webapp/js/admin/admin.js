const nextTermUrl = '/api/admin/term/next';
const registerUserUrl = '/api/admin/user/register';
const registerCourseUrl = '/api/admin/course/register';
const usersUrl = '/api/admin/user/list';
const badgeTemp = '<span class="badge badge-primary badge-pill" style="margin: auto 3px"></span>';

$(document).ready(function () {
    init();
});

function getAllCourses() {
    return $.ajax({
        method: 'GET',
        url: coursesUrl,
        cache: false
    }).error(function(jqXHR) {
        console.log(jqXHR);
        dangerModal("An error occurred while getting course list.");
    });
}

function getAllUsers() {
    return $.ajax({
        method: 'GET',
        url: usersUrl,
        cache: false
    });
}

function init() {
    $.LoadingOverlay("show");

    // Clear Local Storage
    localStorage.clear();
    $('input').val('');


    $.when(initCurrentTerm(), getAllCourses(), getAllUsers()).done(function(term, courseData, userData) {
        $.LoadingOverlay("hide");
        localStorage.setItem('currentTerm', term[0].semester.term);
        localStorage.setItem('currentYear', term[0].semester.year);
        var courses = courseData[0].results.courses;
        localStorage.setItem('courses', JSON.stringify(courses));
        localStorage.setItem('users', JSON.stringify(userData[0]));

        updateTermInfo();
        initNextTerm();
        initUser();
        initCourse();
        initUpdateCourses();
        initUpdateUsers()

    });
}

/*************************************/
/*           Current Term            */
/*************************************/
function initCurrentTerm() {
    // Get current term information on load
    return $.ajax({
        url: currentTermUrl,
        method: 'GET',
        cache: false
    });
}

function displayAcademicTerm() {
    $('#current-term-link').click();
}

function updateTermInfo() {
    $('#current-term').text(localStorage.getItem('currentTerm'));
    $('#current-year').text(localStorage.getItem('currentYear'));
}

/*************************************/
/*             Next Term             */
/*************************************/
function initNextTerm() {
    $('#next-term-link').click(function(e) {
        e.preventDefault();
        displayNextTerm();
    });

    $('#start-next-term-button').click(function() {
        beginNextTerm();
    });

    $('#stay-term-button').click(function() {
        displayAcademicTerm();
    });
}

function displayNextTerm() {
  $('#next-term-prompt').show();
  $('#next-term-status').hide();
}

function beginNextTerm() {
    $('#next-term-prompt').hide();
    $('#next-term-status')
        .show()
        .html('<i class="fa fa-circle-o-notch fa-spin"> </i> Starting next term');

    $.ajax({
        url: nextTermUrl,
        method: 'POST'
    }).success(function(data) {
        localStorage.setItem('currentTerm', data.semester.term);
        localStorage.setItem('currentYear', data.semester.year);
        console.log(localStorage);
        updateTermInfo();
        var message = localStorage.getItem('currentTerm') + ' ' + localStorage.getItem('currentYear')+ ' has begun.';
        successModal(message);
        displayAcademicTerm();
    }).error(function (jqXHR, textStatus) {
        console.log(jqXHR);
        console.log(textStatus);
    });
}


/*******************************/
/*          Add User           */
/*******************************/
function initUser() {
    $('#user-register-btn').click(function(e) {
        e.preventDefault();
        registerUser();
    });
}

function registerUser() {
    var name = $('#user-name').val();
    var address = $('#user-address').val();
    var number = $('#user-number').val();
    var instructorChecked = $('input[name="instructor-role-check"]:checked').length;
    var studentChecked = $('input[name="student-role-check"]:checked').length;
    var userId;

    if (!name || !address || !number) {
        dangerModal('Please enter a name, address, and phone number.');
        return;
    }

    if (!instructorChecked && !studentChecked) {
        dangerModal('Please select at least one role.');
        return;
    }

    var data = {
        "name": name,
        "address": address,
        "phoneNumber": number,
        "roles": []
    };

    if (instructorChecked) {
        data.roles.push("INSTRUCTOR");
    }

    if (studentChecked) {
        data.roles.push("STUDENT");
    }

    $.ajax({
        url: registerUserUrl,
        method: 'POST',
        data: JSON.stringify(data),
        contentType: 'application/json'
    }).success(function (user) {
        console.log(user);
        userId = user.id;
        init();
        successModal('User has been registered with ID ' + userId);
    }).error(function (jqXHR, textStatus) {
        console.log(jqXHR);
        console.log(textStatus);
        if (jqXHR.responseJSON.status === 400) {
            dangerModal("User not registered. Please enter a value for all fields.");
        } else {
            dangerModal("Unable to register user. Please contact an administrator for more information.");
        }
    });
}

/*************************************/
/*          Add Course           */
/*************************************/
function initCourse() {
    $('#course-register-btn').click(function(e) {
        e.preventDefault();
        registerCourse();
    });

    $('#add-course-list').html('');
    const li = '<li class="list-group-item justify-content-between">';
    const label = '<label class="form-check-label" style="font-size: 1rem">';
    const input = '<input class="form-check-input prereqs-check" type="checkbox">';
    var courses = JSON.parse(localStorage.getItem('courses'));

    for (var j in courses) {
        const course = courses[j];
        const courseLabel = $(label).clone();
        const courseInput = $(input).clone();
        courseInput.val(course.courseID);

        const courseReportUrl = '/course.html?id=' + course.courseID;
        const a = $('<a>').attr('href', courseReportUrl);
        const badge = $(courseBadge).clone().text('View');
        a.append(badge);
        courseLabel.append(courseInput).append(' ' + course.courseID + ' ' + course.courseName);
        $('#add-course-list').append($(li).clone().append(courseLabel).append(a));
    }
}

function registerCourse() {
    var name = $('#course-name').val();
     var data = {
         "courseName": name,
         "prereqsCourses": [],
         "termsOffered": []
     };

     $('.term-check:checked').each(function() {
         data.termsOffered.push($(this).attr('name'));
     });

     $('.prereqs-check:checked').each(function() {
         var prereq = {};
         prereq.courseID = $(this).val();
         data.prereqsCourses.push(prereq);
     });

     if (!name || data.prereqsCourses.termsOffered < 1) {
         dangerModal('Please enter a course name and select at least one term');
         return;
     }

    $.ajax({
        url: registerCourseUrl,
        method: 'POST',
        data: JSON.stringify(data),
        contentType: 'application/json'
    }).success(function (data) {
        console.log(data);
        var message = 'Course registered';
        init();
        successModal(message);
        //var footer = '<a href="/course.html?id=' + data.course.courseID + '" class="btn btn-primary">View</a>';
        //successModal('Course registered.', footer);
    }).error(function (jqXHR, textStatus) {
        console.log(jqXHR);
        console.log(textStatus);
        if (jqXHR.responseJSON.status === 400) {
            dangerModal("Course not registered. Please enter a value for all fields.");
        } else {
            dangerModal("Unable to register instructor. Please contact an administrator for more information.");
        }
    });
}

function initUpdateUsers() {
    var users = JSON.parse(localStorage.getItem('users'));

    $('#update-user-ul').html('');
    const li = '<li class="list-group-item justify-content-between" style="padding: 1.5rem">';
    const label = '<label class="custom-control custom-radio" style="font-size: 1rem">';
    const input = '<input class="custom-control-input" type="radio" name="update-user-radio">';
    const indicator = '<span class="custom-control-indicator">';
    const description = '<span class="custom-control-description">';

    for (var j in users) {
        const userLi = $(li).clone();
        const user = users[j];
        const userLabel = $(label).clone();
        const userInput = $(input).clone();
        userInput.val(user.id);
        const userDescription = $(description).clone().text(user.id + ' ' + user.name);
        const reports = $('<div>');

        userLabel.append(userInput).append($(indicator).clone()).append(userDescription);
        $('#update-user-ul').append(userLi.append(userLabel).append(reports));

        if (user.roles.includes('STUDENT')) {
            const studentReportUrl = '/student.html?id=' + user.id;
            const studentLink = $('<a>').attr('href', studentReportUrl);
            const studentBadge = $(badgeTemp).clone().html(' Student <br> Report');
            studentLink.append(studentBadge);
            reports.append(studentLink);
        }

        if (user.roles.includes('INSTRUCTOR')) {
            const instructorReportUrl = '/instructor.html?id=' + user.id;
            const instructorLink = $('<a>').attr('href', instructorReportUrl);
            const instructorBadge = $(badgeTemp).clone().html(' Instructor <br> Report');
            instructorLink.append(instructorBadge);
            reports.append(instructorLink);
        }
    }
}

function selectUserUpdate() {
    var userId = $('input[name="update-user-radio"]:checked').val();
    var user = $.grep(JSON.parse(localStorage.getItem('users')), function(e) { return e.id === userId })[0];

    if (!user) {
        dangerModal('Please select a user.');
        return;
    }

    $('#update-user-form').show().data('user-id', userId);
    $('#update-user-name').val(user.name)
    $('#update-user-address').val(user.address);
    $('#update-user-number').val(user.phoneNumber);

    $('#update-user-form').find('i').each(function() {
        $(this).addClass('active');
    });

    $('#update-user-form').find('label').each(function() {
        $(this).addClass('active');
    });

    $('#update-user-list').hide();
    $('#update-user-form').show();
}

function updateUser() {
    var name = $('#update-user-name').val();
    var address = $('#update-user-address').val();
    var number = $('#update-user-number').val();

    if (!name || !address || !number) {
        dangerModal('Please enter a name, address, and number.');
        return;
    }

    var user = {
        id: $('#update-user-form').data('user-id'),
        name: name,
        address: address,
        phoneNumber: number,
        roles: []
    };

    $.LoadingOverlay('show');
    $.ajax({
        url: '/api/admin/user/update',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(user)
    }).success(function() {
        $.when(getAllUsers()).done(function(userData) {
            localStorage.setItem('users', JSON.stringify(userData));
            initUpdateUsers();
            cancelUserUpdate();
            $('#update-user-link').click();
        });
        successModal('User updated');
    }).done(function() {
        $.LoadingOverlay('hide');
    });
}

function cancelUserUpdate() {
    $('#update-user-list').show();
    $('#update-user-form').hide()
}

function initUpdateCourses() {
    var courses = JSON.parse(localStorage.getItem('courses'));

    $('#update-course-ul').html('');
    $('#update-prereqs-list').html('');

    const li = '<li class="list-group-item justify-content-between" style="padding: 1.5rem">';
    const label = '<label class="custom-control custom-radio" style="font-size: 1rem">';
    const input = '<input class="custom-control-input" type="radio" name="update-course-radio">';
    const indicator = '<span class="custom-control-indicator">';
    const description = '<span class="custom-control-description">';


    const pInput = '<input class="form-check-input" type="checkbox" name="update-prereq-radio">';
    const prereqLabel = '<label class="form-check-label" style="margin: auto 10px;">';


    for (var j in courses) {
        const courseLi = $(li).clone();
        const course = courses[j];
        const courseLabel = $(label).clone();
        const courseInput = $(input).clone();
        courseInput.val(course.courseID);
        const courseDescription = $(description).clone().text(course.courseID + ' ' + course.courseName);
        const reports = $('<div>');

        courseLabel.append(courseInput).append($(indicator).clone()).append(courseDescription.clone());
        $('#update-course-ul').append(courseLi.append(courseLabel).append(reports));

        const courseReportUrl = '/course.html?id=' + course.courseID;
        const a = $('<a>').attr('href', courseReportUrl);
        const badge = $(courseBadge).clone().html('Course <br> Report');
        courseLi.append(a.append(badge));

        // Build prereq list
        const prereqInput = $(pInput).clone();
        prereqInput.val(course.courseID);
        $('#update-prereqs-list')
            .append($(li).clone()
                .append($(prereqLabel).clone()
                    .append(prereqInput)
                    .append(courseDescription.clone())));

    }

    $(document).on('change', 'input[name="update-prereq-radio"]', function() {
        $(this).toggleClass('prereq-updated');
    });
}

function selectCourseUpdate() {
    var courseId = $('input[name="update-course-radio"]:checked').val();
    if (!courseId) {
        dangerModal('Please select a course.');
        return;
    }

    $.ajax({
        method: 'GET',
        url: '/api/course/' + courseId,
        cache: false
    }).success(function(courseReport) {
        var course = courseReport.course;
        $('input[name="update-prereq-radio"][value="' + courseId + '"]').attr("disabled", "disabled");

        for (var i in course.prereqsCourses) {
            var prereqCourse = course.prereqsCourses[i];
            $('input[name="update-prereq-radio"][value="' + prereqCourse.courseID + '"]').attr('checked','checked');

        }

        $('#update-course-form').show().data('course-id', courseId);
        $('#update-course-name').val(course.courseName)

        $('#update-course-prefix').addClass('active');

        $('#update-course-form').find('label[for="user-address"]').each(function() {
            $(this).addClass('active');
        });

        $('#update-course-list').hide();
        $('#update-course-form').show();
    });
}

function updateCourse() {
    const name = $('#update-course-name').val();
    if (!name) {
        dangerModal('Course name cannot be blank');
        return;
    }

    var prereqs = [];
    $('.prereq-updated').each(function() {
       var action = $(this).is(':checked') ? 'ADD' : 'REMOVE';
       var prereq = {
           courseID: $(this).val() + '-' + action
       };
       prereqs.push(prereq);
    });

    var data = {
        courseID: $('#update-course-form').data('course-id'),
        courseName: name,
        prereqsCourses: prereqs
    };

    $.ajax({
        url: '/api/admin/course/update',
        contentType: 'application/json',
        method: 'POST',
        data: JSON.stringify(data)
    }).success(function(data) {
        $.when(getAllCourses()).done(function(data) {
            localStorage.setItem('courses', JSON.stringify(data.results.courses));
            initUpdateCourses();
            cancelCourseUpdate();
            $('#update-course-link').click();
        });
        successModal('Course updated');
    })
}

function cancelCourseUpdate() {
    $('#update-course-list').show();
    $('#update-course-form').hide()
}