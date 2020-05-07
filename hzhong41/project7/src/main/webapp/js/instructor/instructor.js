const updateEligibleCoursesUrl = '/api/instructor/eligibleCourses';
const getAllInstructorUrl = '/api/instructor/list';
const getInstructorUrl = '/api/instructor/';
const hireInstructorUrl = '/api/instructor/hire';
const registerStudentUrl = '/api/student/register';
const unenrollStudentUrl = '/api/student/unenroll';
const instructorTeachUrl = '/api/instructor/teach';
const unassignUrl = '/api/instructor/unassign';

$(document).ready(function() {
    localStorage.clear();
    init();
});

function init() {
    $.LoadingOverlay("show");
    var promises = [];

    // Get current term information on load
    promises.push($.ajax({
            url: currentTermUrl,
            method: 'GET'
        }).done(function(data) {
            console.log(data);
            localStorage.setItem('currentTerm', data.semester.term);
            localStorage.setItem('currentYear', data.semester.year);
            var id = getUrlParameter('id');
            if (id) {
                loadInstructor(id);
            }
        }));

    promises.push($.ajax({
            url: getAllInstructorUrl,
            method: 'GET',
            cache: false
        }).done(function(data) {
            for (var i in data.results) {
                var instructors = data.results[i];
                const list = $('#find-instructor-list');
                const li = '<li class="list-group-item justify-content-between">';
                const label = '<label class="custom-control custom-radio find-instructor-item">';
                const input = '<input name="find-instructor" type="radio" class="custom-control-input">';
                const indicator = '<span class="custom-control-indicator">';
                const description = '<span class="custom-control-description">';

                for (var i in instructors) {
                    const instructor = instructors[i];
                    const instructorLi = $(li).clone();
                    const instructorLabel = $(label).clone();
                    const instructorInput = $(input).clone().val(instructor.id);
                    const instructorIndicator = $(indicator).clone();
                    const instructorDescription = $(description).clone().text(instructor.id + ' ' + instructor.name);

                    instructorLabel.append(instructorInput).append(instructorIndicator).append(instructorDescription);
                    instructorLi.append(instructorLabel);
                    list.append(instructorLi);
                }
            }
        }).fail(function (jqXHR, errorMessage) {
            console.log(jqXHR);
            dangerModal('An error occurred while retrieving instructor list.');
        }));

    promises.push($.ajax({
            method: 'GET',
            url: coursesUrl
        }).success(function (data) {
            var courses = data.results.courses;
            localStorage.setItem('courses', JSON.stringify(courses));
        }).error(function(jqXHR) {
            console.log(jqXHR);
            dangerModal("An error occurred while getting course list.");
        }));

    $.when(promises).done(function() {
        $.LoadingOverlay("hide");
    });
}

function loadInstructor(id) {

    $.ajax({
        url: getInstructorUrl + id,
        method: 'GET',
        cache: false
    }).done(function(data) {
        console.log(data);
        localStorage.setItem('instructor', JSON.stringify(data.results[id]));
        loadInstructorUI(data.results[id]);

    }).error(function(jqHXR, errorText) {
        if (jqHXR.status === 404) {
            console.log('Instructor ' + id + ' not found.');
            console.log(errorText);
        } else {
            console.log(jqHXR);
            console.log(errorText);
        }
    });
}

/****************************************/
/*         Page Load                    */
/****************************************/
function loadInstructorUI(instructor) {
    $('.content-link').not('#teach-course-link').show();

    loadInstructorDetail(instructor);
    loadInstructorStatus(instructor);
    loadRegisterStudent(instructor);
    loadEligibleCourses(instructor);
    loadAssignCourse();

    if (instructor.active) {
        loadTeachCourse(instructor);
        $('#teach-course-link').show();
    }

    $('#instructor-information-link').click();

    $(document).on('click', '.teach-course-item', function () {
       $('.teach-course-item').parent().removeClass('active');
       $(this).parent().addClass('active');
    });
}

function loadInstructorDetail(instructor) {
    $('#instructor-info-content').find('input').val('');

    $('#instructor-info-content .md-form .prefix').addClass('active');
    $('#instructor-info-content').find('label').addClass('active');
    $('#instructor-detail-id').val(instructor.id);
    $('#instructor-detail-name').val(instructor.name);
    $('#instructor-detail-address').val(instructor.address);
    $('#instructor-detail-number').val(instructor.phoneNumber);

    var status = instructor.active ? 'Hired' : 'On Leave';
    $('#instructor-detail-status').val(status);

    var currentCourse = instructor.courseCurrentTeaching
        ? instructor.courseCurrentTeaching.courseName
        : 'Not currently teaching.';
    $('#instructor-detail-current-course').val(currentCourse);

    if (instructor.eligibleCourses.length > 0) {
        var courses = instructor.eligibleCourses;
        $('#instructor-detail-eligible').text('');
        for (var id in courses) {
            $('#instructor-detail-eligible').append(courses[id].courseID + ': ' + courses[id].courseName + '&#13;&#10;');
        }
    } else {
        $('#instructor-detail-eligible').text('Not eligible for any courses.');
    }

}

function loadInstructorStatus(instructor) {
    var statusStatement;
    var prompt;

    if (instructor.active) {
        $('#instructor-status-link').text('Take Leave Instructor');
        statusStatement = '<p>' + instructor.name + ' is currently hired.</p>';
        prompt = '<p>Take leave ' + instructor.name + '?</p>';
        $('#toggle-status-button').data('hire', false);
    } else {
        $('#instructor-status-link').text('Hire Instructor');
        statusStatement = '<p>' + instructor.name + ' is currently on leave.</p>';
        prompt = '<p>Hire ' + instructor.name + '?</p>';
        $('#toggle-status-button').data('hire', true);
    }

    $('#status-content').html(statusStatement + prompt);
}

function loadRegisterStudent(instructor) {
    var title;
    var body;
    var prompt;

    if (instructor.roles.includes('STUDENT')) {
        $('#register-student-link').text('Unenroll as Student');
        title = 'Unregister as Student';
        body = '<p>' + instructor.name + ' is enrolled as a student.</p>';
        prompt = '<p>Unenroll ' + instructor.name + '?</p>';
        $('#toggle-student-button').data('register', false);
    } else {
        $('#register-student-link').text('Register as Student');
        title = 'Register as Student';
        body = '<p>' + instructor.name + ' is not registered as a student.</p>';
        prompt = '<p>Register ' + instructor.name + '?</p>';
        $('#toggle-student-button').data('register', true);
    }

    $('#register-student-content-title').text(title);
    $('#register-student-content-body').html(body + prompt);
}

function loadTeachCourse(instructor) {
    if (instructor.courseCurrentTeaching) {
        const message = '<li><p>' + instructor.name + ' is already assigned to ' + instructor.courseCurrentTeaching.courseName + '.</p></li>';
        const prompt = '<li><p>Unassign ' + instructor.name + '?</p></li>';
        const yes = '<li><button class="btn btn-info" onclick="unassignInstructor('
            + instructor.id
            + ',' + instructor.courseCurrentTeaching.courseID
            +')">Yes</button>';

        const no = '<button class="btn btn-blue-grey" onclick="displayInfo()">No</button></li>';

        const list = '<ul>' + message + prompt + yes + no + '</ul>';

        $('#teach-course-message').html(list);

    } else {
        const title = '<h4>Select a course to teach: </h4>';
        const teachButton = '<button class="btn btn-info" onclick="teachCourse()">Teach</button>';

        const ul = $('<ul class="list-group">');
        const li = '<li class="list-group-item justify-content-between">';
        const label = '<label class="custom-control custom-radio teach-course-item">';
        const input = '<input name="teach-course" type="radio" class="custom-control-input">';
        const indicator = '<span class="custom-control-indicator">';
        const description = '<span class="custom-control-description" style="font-size: 1rem">';

        for (var i in instructor.eligibleCourses) {
            var course = instructor.eligibleCourses[i];
            var courseLi = $(li).clone();
            var courseLabel = $(label).clone();
            var courseInput = $(input).clone().val(course.courseID);
            var courseIndicator = $(indicator).clone();
            var courseDescription = $(description).clone().text(course.courseID + ' ' + course.courseName);

            const courseReportUrl = '/course.html?id=' + course.courseID + '&instructorId=' + instructor.id;
            const a = $('<a>').attr('href', courseReportUrl);
            const badge = $(courseBadge).clone().text('View');
            a.append(badge);

            courseLabel.append(courseInput).append(courseIndicator).append(courseDescription);
            courseLi.append(courseLabel).append(a);
            ul.append(courseLi);
        }


        $('#teach-course-message').html('')
            .append(title)
            .append(ul)
            .append('<br>')
            .append(teachButton);
    }
}

function loadEligibleCourses() {
    var instructor = JSON.parse(localStorage.getItem('instructor'));
    var courses = JSON.parse(localStorage.getItem('courses'));
    buildEligibleCourses(instructor, courses);
}

function loadAssignCourse() {
    var courses = JSON.parse(localStorage.getItem('courses'));

    var ul = $('#assign-grade-list');

    for (var i in courses) {
        var course = courses[i];
        var li = $('<li class="list-group-item justify-content-between">');
        var label = $('<label class="custom-control custom-radio assign-grade-item">');
        var input = $('<input name="assign-grade" type="radio" class="custom-control-input">').val(course.courseID);
        var indicator = $('<span class="custom-control-indicator">');
        var description = $('<span class="custom-control-description">').text(course.courseID + ': ' + course.courseName);

        ul.append(li.append(label.append(input).append(indicator).append(description)));
    }
}

function buildEligibleCourses(instructor, courses) {
    // Clear out any left over eligible courses.
    $('#eligible-course-list').html('');
    const currentCourse = instructor.courseCurrentTeaching;
    const currentCourseId = currentCourse ? currentCourse.courseID : null;
    var currentIds = [];
    for (var i in instructor.eligibleCourses) {
        currentIds.push(instructor.eligibleCourses[i].courseID);
    }

    const li = '<li class="list-group-item justify-content-between">';
    const label = '<label class="form-check-label" style="font-size: 1rem">';
    const input = '<input class="form-check-input eligible-check" type="checkbox">';

    for (var j in courses) {
        const course = courses[j];
        const courseLabel = $(label).clone();
        const courseInput = $(input).clone();
        courseInput.val(course.courseID);
        if (currentIds.includes(course.courseID)) {
            courseInput.attr('checked', 'checked');
        }

        if (course.courseID === currentCourseId) {
            courseInput.attr('disabled', 'disabled');
        }

        const courseReportUrl = '/course.html?id=' + course.courseID + '&instructorId=' + instructor.id;
        const a = $('<a>').attr('href', courseReportUrl);
        const badge = $(courseBadge).clone().text('View');
        a.append(badge);
        courseLabel.append(courseInput).append(' ' + course.courseName);
        $('#eligible-course-list').append($(li).clone().append(courseLabel).append(a));
    }
}

function displayInfo() {
    $('#instructor-information-link').click();
}

/****************************************/
/*         Find Instructor              */
/****************************************/
function findInstructor() {
    const id = $('input[name="find-instructor"]:checked').val();
    if (!id) {
        dangerModal('Please select an Instructor.');
        return;
    }

    $.ajax({
        method: 'GET',
        url: getInstructorUrl + id
    }).success(function (data) {
        console.log(data);
        localStorage.setItem('instructor', JSON.stringify(data.results[id]));
        loadInstructorUI(data.results[id]);
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal('Instructor with ID ' + id + ' not found.');
    });
}

/****************************************/
/*         Instructor Status            */
/****************************************/
function toggleInstructorStatus() {
    var instructor = JSON.parse(localStorage.getItem('instructor'));
    instructor.active = $('#toggle-status-button').data('hire');
    $.ajax({
        method: 'POST',
        url: hireInstructorUrl,
        data: JSON.stringify(instructor),
        contentType: 'application/json;charset=UTF-8'
    }).success(function (data) {
        console.log(data);
        successModal(data.message);
        loadInstructor(instructor.id);
    }).fail(function (jqXHR, errorMessage) {
       console.log(jqXHR);
       console.log(errorMessage);
       dangerModal("An error occurred while updating the instructor's status. Please contact an administrator.");
    });
}

/****************************************/
/*            Student Role              */
/****************************************/
function toggleStudentRole() {
    var instructor = JSON.parse(localStorage.getItem('instructor'));

    $.ajax({
        url: $('#toggle-student-button').data('register') ? registerStudentUrl : unenrollStudentUrl,
        method: 'POST',
        contentType: 'application/json',
        data: localStorage.getItem('instructor')
    }).success(function (data) {
        console.log(data);
        loadInstructor(instructor.id);
        successModal(instructor.name + ' student role updated.');
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("An error occurred while updating the instructor's student role. Please contact an administrator.");
    });
}

/****************************************/
/*            Eligible Courses          */
/****************************************/
function updateEligibleCourses() {
    var selected = [];
    var instructor = JSON.parse(localStorage.getItem('instructor'));
    $('.eligible-check:checked').each(function() {
        const course = {
            courseID: $(this).val(),
        };
        selected.push(course);
    });
    instructor.eligibleCourses = selected;

    $.ajax({
        url: updateEligibleCoursesUrl,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(instructor)
    }).success(function(data) {
        console.log(data);
        loadInstructor(instructor.id);
        successModal('Eligible courses updated successfully!');
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal('Error updating eligible courses. Please contact an administrator for assistance.');
    });
}

/*************************************************/
/*            Teach Course/Unassign              */
/*************************************************/
function unassignInstructor(instructorId, courseId) {
    const courseOffering = {
        instructorID: instructorId,
        courseID: courseId,
        year: localStorage.getItem('currentYear'),
        term: localStorage.getItem('currentTerm')
    };

    $.ajax({
        method: 'POST',
        url: unassignUrl,
        data: JSON.stringify(courseOffering),
        contentType: 'application/json'
    }).success(function (data) {
        console.log(data);
        loadInstructor(instructorId);
        successModal("Instructor has been unassigned from course.");
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("An error occurred while unassigning instructor. Please contact an administrator.");
    });
}

function teachCourse() {
    var instructor = JSON.parse(localStorage.getItem('instructor'));
    var courseId = $('input[name="teach-course"]:checked').val();
    if (!courseId) {
        dangerModal('Please select a course to teach.');
        return;
    }
    var courseOffering = {
        instructorID: instructor.id,
        courseID: courseId,
        year: localStorage.getItem('currentYear'),
        term: localStorage.getItem('currentTerm')
    };

    $.ajax({
        method: 'POST',
        url: instructorTeachUrl,
        contentType: 'application/json',
        data: JSON.stringify(courseOffering)
    }).success(function (data) {
        console.log(data);
        loadInstructor(instructor.id);
        successModal(instructor.name + ' is now teaching.');
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("An error occurred while updating the instructor's student role. Please contact an administrator.");
    });
}

/*************************************************/
/*                 Assign Grade                  */
/*************************************************/
function getCourseRecords() {
    const courseID = $('input[name="assign-grade"]:checked').val();
    const term = $('#assign-grade-term option:selected').val();
    const year = $('#assign-grade-year').val();

    if (!courseID || !term || !year) {
        dangerModal('Please enter valid data and select a course.');
        return;
    }

    const json = {
        courseID: courseID,
        term: term,
        year: year
    };

    $.ajax({
        method: 'POST',
        url: '/api/course/records',
        contentType: 'application/json',
        data: JSON.stringify(json)
    }).success(function(data) {
        console.log(data);
        loadCourseRecords(data.records, courseID);
        $('#assign-grade-semester').find('small[name="term"]').text(term);
        $('#assign-grade-semester').find('small[name="year"]').text(year);
    }).error(function(jqXHR) {
       if (jqXHR.status === 404) {
           dangerModal('No records found.');
           return;
       } else {
           dangerModal(jqXHR.responseJSON.message);
       }
    });
}

function loadCourseRecords(records, courseID) {
    $('#course-records-list').show();
    var list = $('#course-record-assign');

    for (var i in records) {
        var record = records[i];

        var groupItem = $('<button class="list-group-item">');
        var idInput = $('<input type="hidden" class="id-input">').val(record.student.id);
        var nameInput = $('<input type="text"readonly class="form-control name-input">').val(record.student.name);

        var gradeInput = $('<input type="text" class="form-control grade-input" maxlength="1">').val(record.grade);

        var commentInput = $('<input type="text" class="form-control comment-input">').val(record.comment);

        var updateBox = $('<input type="checkbox" class="form-control update-input">');

        list.append(groupItem.clone()
                .append($('<label>').append('Student Name').append(nameInput))
                .append($('<label>').append('Grade').append(gradeInput))
                .append($('<label>').append('Comment').append(commentInput))
                .append($('<label>').append('Update').append(updateBox))
                .append(idInput));
    }
    var course = $.grep(JSON.parse(localStorage.getItem('courses')), function(e) { return e.courseID === courseID; })[0];
    $('#assign-grade-course-id').val(courseID);
    $('#assign-grade-course-title').text(course.courseName);

}

function assignGrade() {
    const instructor = JSON.parse(localStorage.getItem('instructor'));

    var records = $('.update-input:checked');
    var assignments = [];

    for (var i = 0; i < records.length; i++) {
        var btnGroup = $(records[i]).parent().parent();
        var studentId = btnGroup.find('input[type="hidden"]').val();
        if (studentId === instructor.id) {
            dangerModal("Instructor cannot assign grade to self.");
            continue;
        }
        var grade = btnGroup.find('.grade-input').val();
        var comment = btnGroup.find('.comment-input').val();

        var data = {
            studentID: studentId,
            courseID: $('#assign-grade-course-id').val(),
            instructorID: instructor.id,
            grade: grade,
            comment: comment,
            year: $('small[name="year"]').text(),
            term: $('small[name="term"]').text()
        };
        assignments.push(data);
    }

    $.ajax({
        method: 'POST',
        url: '/api/instructor/assignGrade',
        contentType: 'application/json',
        data: JSON.stringify(assignments)
    }).success(function(data) {
        console.log(data);
        successModal("Successfully assigned all grades!");
        loadInstructor(instructor.id);
    }).error(function(jqXHR) {
        console.log(jqXHR);
        dangerModal("Could not update records for: " + jqXHR.resopnseJSON.message);
    })
}