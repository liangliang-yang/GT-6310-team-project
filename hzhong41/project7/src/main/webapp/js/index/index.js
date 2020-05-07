var initDataUrl = '/api/admin/initialize';
var initSpinner = '#init-spinner';

$(document).ready(function() {

    $('#init-data-button').click(function(e) {
        e.preventDefault();
        if ($('#init-data-button').hasClass('active')) {
            return;
        }
        confirmInitialize();
    });
});

function confirmInitialize() {
    const message = '<p>Initializing data will reset all data to an initial state. ' +
        'All changes in the database will be lost.</p>' +
        '<p>Do you want to continue?</p>';
    const yes = '<button class="btn btn-danger" onclick="loadData()" data-dismiss="modal">Continue</button>';
    const no = '<button class="btn btn-blue-grey" data-dismiss="modal">Cancel</button>';
    const footer = yes + no;
    dangerModal(message, footer);
}

function loadData() {
    $('#init-data-button').addClass('active');
    toggleSpinner(initSpinner);
    $.ajax({
        url: initDataUrl,
        method: 'POST',
        contentType: 'application/json'
    }).done(function(data) {
        successModal(data);
    }).error(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal(jqXHR.responseJSON.message);
    }).always(function() {
        toggleSpinner(initSpinner);
        $('#init-data-button').removeClass('active');
    })
}