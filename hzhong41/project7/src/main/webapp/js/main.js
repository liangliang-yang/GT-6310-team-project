const currentTermUrl = '/api/admin/term';
const coursesUrl = '/api/course/list';

const courseBadge = '<span class="badge badge-primary badge-pill"></span>';

$(document).ready(function() {
    $('.content-link').click(function(e) {
        e.preventDefault();
        $('.content-link').removeClass('active');
        $(this).addClass('active');
        var contentElement = '#' + $(this).data('content-box');
        displayContent(contentElement);
    });
});

function displayContent(element) {
    $('.content-box').hide();
    $(element).show();
}

/*************************************/
/*             Utilities             */
/*************************************/
var dangerModal = function(message, footer, title) {
    if (title) {
        $('#modal-title').text(title);
    } else {
        $('#modal-title').text('Warning!');
    }

    $('.modal-dialog').removeClass('modal-success').addClass('modal-danger');
    $('#message-modal-status').html(message);

    if (footer === null || footer === undefined) {
        $('.modal-footer').html('<button class="btn btn-info" data-dismiss="modal">Dismiss</button>');
    } else {
        $('.modal-footer').html(footer);
    }

    if (!$('#message-modal').hasClass('show')) {
        $('#message-modal').modal('show');
    }
};

var successModal = function(message, footer, title) {
    if (title) {
        $('#modal-title').text(title);
    } else {
        $('#modal-title').text('Success!');
    }

    $('.modal-dialog').addClass('modal-success').removeClass('modal-danger');
    $('#message-modal-status').text(message);

    if (footer === null || footer === undefined) {
        $('.modal-footer').html('<button class="btn btn-success" data-dismiss="modal">Dismiss</button>');
    } else {
        $('.modal-footer').html(footer);
    }

    if (!$('#message-modal').hasClass('show')) {
        $('#message-modal').modal('show');
    }

};

function toggleSpinner(element) {
    $(element).toggleClass('fa-circle-o-notch fa-spin fa');
}

function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    var results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
}