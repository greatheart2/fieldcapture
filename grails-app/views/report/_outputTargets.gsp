<html>

<head>
    <style type="text/css">
    #outputTargets th {
        white-space: normal;
    }

    #outputTargets td {
        white-space: normal;
    }

    #outputTargets table {
        width:100%;
    }
    #outputTargets_wrapper{
        overflow-x: auto;
    }
    div.dataTables_wrapper div.dataTables_filter label {
        font-weight: normal !important;
        white-space: nowrap !important;
        text-align: left !important;
        margin-left: -0.9rem !important;
    }
    div.dt-buttons {
        position: relative;
        float: left;
        margin-left: -14px;
    }
    table#outputTargets {
        margin-left: -0.9rem;
    }
    </style>
</head>
<table id="outputTargets" class="table table-striped">

</table>
    <script type="text/javascript">
    $(function() {


        var date = moment().format('YYYY-MM-DD');
        var scores = <fc:modelAsJavascript model="${scores}"></fc:modelAsJavascript>;
        var columns =  [
            {title:'Output Target measure', width:'200px', data:'score'}
        ];
        <g:each in="${programs}" var="program">
            columns.push({title:"${program} - Target", data:"${program} - Target", width:'50px'});
            columns.push({title:"${program} - Delivered", data:"${program} - Value", width:'50px'});
        </g:each>

        $('#outputTargets').dataTable({
            "data": scores,
            "order":[[0, 'asc']],
            "autoWidth":false,
            "columns": columns,
            "dom": 'Blfrtip',

            buttons: [
                'copy',
                {
                    extend: 'excel',
                    title: 'Output targets '+date
                },
                {
                    extend: 'pdf',
                    title: 'Output targets '+date
                },
                {
                    extend: 'csv',
                    title: 'Output targets '+date
                }
            ]
        });


    });


</script>


</html>
