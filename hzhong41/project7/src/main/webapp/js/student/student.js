const getAllStudentsUrl = '/api/student/list';
const getStudentUrl = '/api/student/';
const requestCourseUrl = '/api/student/requestCourse';
const dropCourseUrl = '/api/student/dropCourse';
const instructorsUrl = '/api/instructor/list';

$(document).ready(function() {
   localStorage.clear();
   init();

   $(document).on('click', '.find-student-item', function() {
       $('.find-student-item').parent().removeClass('active');
       $(this).parent().addClass('active');
   })
});

function init() {

    // Get all students
    $.ajax({
        url: getAllStudentsUrl,
        method: 'GET',
        cache: false
    }).done(function(data) {
        console.log(data);
        buildStudentList(data.results.students);
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("An error occurred while retrieving student list.");
    });

    // Get current term information on load
    $.ajax({
        url: currentTermUrl,
        method: 'GET',
        cache: false
    }).done(function(data) {
        console.log(data);
        localStorage.setItem('currentTerm', data.semester.term);
        localStorage.setItem('currentYear', data.semester.year);
        var id = getUrlParameter('id');
        if (id) {
            loadStudent(id);
        }
    });

    // Get all courses
    $.ajax({
        url: coursesUrl,
        method: 'GET',
        cache: false
    }).done(function(data) {
        console.log(data.results.courses);
        localStorage.setItem('courses', JSON.stringify(data.results.courses));
    }).fail(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("An error occurred while retrieving course list.");
    });

    // Get all instructors
    $.ajax({
        url: instructorsUrl,
        method: 'GET',
        cache: false
    }).done(function(data) {
        localStorage.setItem('instructors', JSON.stringify(data.results.instructors));
    }).fail(function(jqXHR) {
        console.log(jqXHR);
        dangerModal('Unexpected error occurred while retrieving instructors.');
    })
}

function loadStudent(id) {

    $.when(getStudentDetail(id), loadStudentWaitList(id)).done(function(detailResult, waitListResult) {
       localStorage.setItem('student', JSON.stringify(detailResult[0].results.student));
       localStorage.setItem('waitlist', JSON.stringify(waitListResult[0]));
       loadStudentUI(detailResult[0].results);
       displayStudent()
    });
}

function getStudentDetail(id) {
    return  $.ajax({
        url: getStudentUrl + id,
        method: 'GET',
        cache: false
    }).error(function(jqHXR, errorText) {
        if (jqHXR.status === 404) {
            console.log('Student ' + id + ' not found.');
            console.log(errorText);
            dangerModal('Student ' + id + ' does not exist.');
        } else {
            console.log(jqHXR);
            console.log(errorText);
        }
    });
}

function loadStudentWaitList(id) {
    return $.ajax({
        url: '/api/student/waitlist/' + id,
        method: 'GET',
        cache: false
    });
}



function loadStudentUI(studentReport) {
    $('.content-link').show();

    loadStudentDetail(studentReport);
    loadRequestCourse(studentReport);
    if (studentReport.currentCourses && studentReport.currentCourses.length > 0) {
        loadDropCourse(studentReport);
    }
    loadAcademicRecord(studentReport);
}

function buildStudentList(students) {
    $('#find-student-list').html('');
    const ul = $('#find-student-list');
    const li = '<li class="list-group-item justify-content-between">';
    const label = '<label class="custom-control custom-radio find-student-item">';
    const input = '<input name="find-student" type="radio" class="custom-control-input">';
    const indicator = '<span class="custom-control-indicator">';
    const description = '<span class="custom-control-description">';

    for (var i in students) {
        const student = students[i];
        const studentLi = $(li).clone();
        const studentLabel = $(label).clone();
        const studentInput = $(input).clone().val(student.id);
        const studentIndicator = $(indicator).clone();
        const studentDescription = $(description).clone().text(student.id + ' ' + student.name);

        studentLabel.append(studentInput).append(studentIndicator).append(studentDescription);
        studentLi.append(studentLabel);
        ul.append(studentLi);
    }
}

function loadStudentDetail(studentReport) {
    $('#student-info-content .md-form .prefix').addClass('active');
    $('#student-info-content').find('label').addClass('active');
    $('#student-detail-id').val(studentReport.student.id);
    $('#student-detail-name').val(studentReport.student.name);
    $('#student-detail-address').val(studentReport.student.address);
    $('#student-detail-number').val(studentReport.student.phoneNumber);

    $('#student-detail-current-courses').html('');

    for (var i in studentReport.currentCourses) {
        var course = studentReport.currentCourses[i];
        $('#student-detail-current-courses').append(course.courseID + ': ' + course.courseName);
    }

    $('#student-detail-waitlist').html('');

    var waitlist = JSON.parse(localStorage.getItem('waitlist'));
    var courses = JSON.parse(localStorage.getItem('courses'));
    for (var j in waitlist) {
        var request = waitlist[j];
        var courseId = request.courseID;
        var courseDetail = $.grep(courses, function(e) { return e.courseID === courseId })[0];
        $('#student-detail-waitlist').append(courseDetail.courseID + ' ' + courseDetail.courseName + '&#013; &#010;');
    }
}

function loadRequestCourse() {
    var courses = JSON.parse(localStorage.getItem('courses'));
    var student = JSON.parse(localStorage.getItem('student'));

    $('#request-course-list').html('');
    const ul = $('#request-course-list');
    const li = '<li class="list-group-item justify-content-between">';
    const label = '<label class="custom-control custom-radio request-course-item">';
    const input = '<input name="request-course" type="radio" class="custom-control-input">';
    const indicator = '<span class="custom-control-indicator">';
    const description = '<span class="custom-control-description">';

    for (var i in courses) {
        const course = courses[i];
        const courseLi = $(li).clone();
        const courseLabel = $(label).clone();
        const courseInput = $(input).clone().val(course.courseID);
        const courseIndicator = $(indicator).clone();
        const courseDescription = $(description).clone().text(course.courseName);

        const courseReportUrl = '/course.html?id=' + course.courseID + '&studentId=' + student.id;
        const a = $('<a>').attr('href', courseReportUrl);
        const badge = $(courseBadge).clone().text('View');
        a.append(badge);

        courseLabel.append(courseInput).append(courseIndicator).append(courseDescription);
        courseLi.append(courseLabel).append(a);
        ul.append(courseLi);
    }
}

function loadDropCourse(studentReport) {

    $('#drop-course-list').html('');
    const ul = $('#drop-course-list');
    const li = '<li class="list-group-item justify-content-between">';
    const label = '<label class="custom-control custom-radio drop-course-item">';
    const input = '<input name="drop-course" type="radio" class="custom-control-input">';
    const indicator = '<span class="custom-control-indicator">';
    const description = '<span class="custom-control-description">';

    for (var i in studentReport.currentCourses) {
        const course = studentReport.currentCourses[i];
        const courseLi = $(li).clone();
        const courseLabel = $(label).clone();
        const courseInput = $(input).clone().val(course.courseID);
        const courseIndicator = $(indicator).clone();
        const courseDescription = $(description).clone().text(course.courseName);

        const courseReportUrl = '/course.html?id=' + course.courseID + '&studentId=' + studentReport.student.id;
        const a = $('<a>').attr('href', courseReportUrl);
        const badge = $(courseBadge).clone().text('View');
        a.append(badge);

        courseLabel.append(courseInput).append(courseIndicator).append(courseDescription);
        courseLi.append(courseLabel).append(a);
        ul.append(courseLi);
    }
}

function loadAcademicRecord(studentReport) {
    if (studentReport.academicRecord && studentReport.academicRecord.length > 0) {
        $('#no-records').hide();
    }

    $('#academic-record-list').html('');
    const ul = $('#academic-record-list');
    const li = '<li class="list-group-item justify-content-between">';
    const titleDiv = '<div class="d-flex w-100 justify-content-between">';
    const courseName = '<h5 class=mb-1">';
    const attemptDate = '<small>';
    const div = '<div class="mb-1">';

    const courses = JSON.parse(localStorage.getItem('courses'));
    const instructors = JSON.parse(localStorage.getItem('instructors'));

    for (var i in studentReport.academicRecord) {
        const record = studentReport.academicRecord[i];
        const course = $.grep(courses, function(e){ return e.courseID === record.courseID; })[0];

        const recordLi = $(li).clone();
        const recordTitle = $(titleDiv).clone();
        const recordName = $(courseName).clone().text(record.courseID + ': ' + course.courseName);
        const recordAttempt = $(attemptDate).clone().text(record.term + ' ' + record.year);
        const recordGrade = $(div).clone().text('Grade: ' +record.grade);

        const instructor = $.grep(instructors, function(e) { return e.id === record.instructorID})[0];

        var instructorDiv;
        if (instructor && instructor.id !== 0) {
            instructorDiv = $(div).clone().text('Instructor: ' + instructor.name);
        } else {
            instructorDiv = '';
        }

        var recordComment;
        if (record.comment && record.comment !== '') {
            recordComment = $(div).clone().text('Comment: ' + record.comment);
        } else {
            recordComment = '';
        }

        recordLi.append(recordTitle.append(recordName).append(recordAttempt))
            .append(recordGrade)
            .append(instructorDiv)
            .append(recordComment);
        ul.append(recordLi);
    }

}

function displayStudent() {
    $('#student-information-link').click();
}

function findStudent() {
    var studentId = $('input[name="find-student"]:checked').val();
    loadStudent(studentId);
}

function requestCourse() {
    var courseId = $('input[name="request-course"]:checked').val();
    if (!courseId && courseId !== '') {
        dangerModal('Please select a course.');
        return;
    }

    const student = JSON.parse(localStorage.getItem('student'));
    const courseRequest = {
        year: localStorage.getItem('currentYear'),
        term: localStorage.getItem('currentTerm'),
        studentID: student.id,
        courseID: courseId
    };

    $.ajax({
        method: 'POST',
        contentType: 'application/json',
        url: requestCourseUrl,
        data: JSON.stringify(courseRequest)
    }).success(function (data) {
        console.log(data);
        loadStudent(student.id);
        successModal('Successfully enrolled.');
    }).error(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        dangerModal(jqXHR.responseJSON.message);
    });
}

function dropCourse() {
    var courseId = $('input[name="drop-course"]:checked').val();
    if (!courseId && courseId !== '') {
        dangerModal('Please select a course.');
        return;
    }

    const student = JSON.parse(localStorage.getItem('student'));
    const dropRequest = {
        year: localStorage.getItem('currentYear'),
        term: localStorage.getItem('currentTerm'),
        studentID: student.id,
        courseID: courseId,
    };

    $.ajax({
        method: 'POST',
        contentType: 'application/json',
        url: dropCourseUrl,
        data: JSON.stringify(dropRequest)
    }).success(function (data) {
        console.log(data);
        loadStudent(student.id);
        successModal('Successfully dropped course.');
    }).error(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        dangerModal(jqXHR.responseJSON.message);
    });
}