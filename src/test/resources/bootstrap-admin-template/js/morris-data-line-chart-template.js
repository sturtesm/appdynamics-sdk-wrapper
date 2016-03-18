$(function() {

    Morris.Line({
        element: 'morris-area-chart',
        data: $morris_area_data_placeholder,
        xkey: 'period',
        ykeys: $morris_area_ykeys_placeholder,
        labels: $morris_area_ykeys_placeholder,
        pointSize: 2,
        hideHover: 'auto',
        resize: true
    });
});
