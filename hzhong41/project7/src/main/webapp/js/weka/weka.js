const analysisWekaUrl = '/api/weka/analysis';




$(document).ready(function() {
     // Clear Local Storage
    localStorage.clear();
});


/*************************************/
/*             Process Data             */
/*************************************/
function processData() {
    $('#courseRequest-process-btn').addClass('active');
    $.LoadingOverlay('show');
    $.ajax({
        url: analysisWekaUrl,
        method: 'GET',
    }).success(function(data) {
        console.log(data);
        $('#weka-analysis-results').html('Best Rules found: <br>').show();
        for (var i in data) {
            $('#weka-analysis-results').append('<span>' + i + ' ==> ' + data[i] + '</span><br>')
        }
        $('#weka-analysis-results').show();
    }).error(function (jqXHR, errorMessage) {
        console.log(jqXHR);
        console.log(errorMessage);
        dangerModal("There is some error!");
    }).always(function() {
        $.LoadingOverlay('hide');
    })
}

