const loadCourseUrl = '/api/course/';

$(document).ready(function() {
    localStorage.clear();
    init();
});

function init() {
    $("#find-course-id").on('keyup', function (e) {
        if (e.keyCode === 13) {
            findCourse();
        }
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
            loadCourse(id);
        }
    });

    $.ajax({

    })
}

function loadCourse(id) {
    var courseId = id;
    $.ajax({
        url: loadCourseUrl + courseId,
        method: 'GET',
        cache: false
    }).done(function(data) {
        $.ajax({
            url: '/api/course/waitlist/' + courseId,
            method: 'GET'
        }).done(function(count) {
            console.log(data);
            localStorage.setItem('courseReport', JSON.stringify(data));
            localStorage.setItem('waitlist', count);
            loadCourseUI(data);
            displayCourseReport();
        });
    }).error(function(jqXHR, errorText) {
        if (jqXHR.status === 404) {
            console.log('Course ' + id + ' not found.');
            console.log(errorText);
            dangerModal('Course ' + id + ' not found.');
        } else if (jqXHR.status === 400) {
            console.log(jqXHR);
            console.log(errorText);
            dangerModal(id + ' is not a valid course ID.');
        } else {
            console.log(jqXHR);
            console.log(errorText);
            dangerModal('An error occurred while retrieving course ' + id + '. Please contact an administrator.');
        }
    })
}

/****************************************/
/*         Page Load                    */
/****************************************/
function loadCourseUI(courseReport) {
    $('.content-link').show();
    loadCourseDetails(courseReport);
}

function loadCourseDetails(courseReport) {
    var course = courseReport.course;

    $('#course-report-content').find('input').val('');
    $('#course-report-content .md-form .prefix').addClass('active');
    $('#course-report-content').find('label').addClass('active');

    $('#course-detail-id').val(course.courseID);
    $('#course-detail-title').val(course.courseName);

    const currentTerm = localStorage.getItem('currentTerm');
    const status = (course.termsOffered.includes(currentTerm))
        ? 'Offered'
        : 'Not offered this term';
    $('#course-detail-status').val(status);

    $('#course-detail-terms').text('');
    for (var t in course.termsOffered) {
        $('#course-detail-terms').append(course.termsOffered[t] + '&#13;&#10;');
    }

    const availableSeats = courseReport.availableSeats ? courseReport.availableSeats : 0;
    $('#course-detail-seats').val(availableSeats);

    const waitListCount = localStorage.getItem('waitlist') ? localStorage.getItem('waitlist') : 0;
    $('#course-detail-waitlist').val(waitListCount);

    if (course.prereqsCourses.length > 0) {
        var prereqs = course.prereqsCourses;
        $('#course-detail-prereqs').text('');

        for (var p in course.prereqsCourses) {
            var pre = course.prereqsCourses[p]
            $('#course-detail-prereqs').append(pre.courseID + ': ' + pre.courseName + ' &#13;&#10;');
        }
    }
}

function displayCourseReport() {
    $('#course-report-link').click();
}

function findCourse() {
    const id = $('#find-course-id').val();
    loadCourse(id);
}

function returnBack() {
    const instructorId = getUrlParameter('instructorId');
    const studentId = getUrlParameter('studentId');

    if (instructorId && instructorId !== '') {
        window.location.href='/instructor.html?id=' + instructorId;
    } else if (studentId && studentId !== '') {
        window.location.href='/student.html?id=' + studentId;
    } else {
        // If no instructor or student ID, return to the admin page.
        window.location.href='/administrator.html';
    }

}

