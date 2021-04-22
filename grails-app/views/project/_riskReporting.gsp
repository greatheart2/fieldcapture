<!-- ko stopBinding:true -->
<div id="risk-changes-report">
    <h4>Changes to risks and threats</h4>

    <p>Select the date range over which to view the changes to the project risks and threats then press the "Generate Report" button</p>
    <hr/>

    <form class="form-horizontal" id="risks-changes-report">
        <div class="form-group">
            <label class="control-label" for="risks-changes-from-date">From date (baseline for comparison):</label>
            <div class="input-group input-append">
                <fc:datePicker class="form-control form-control-sm dateControl" bs4="bs4" id="risks-changes-from-date" targetField="fromDate.date"/>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label" for="risks-changes-to-date">To date:</label>
            <div class="input-group input-append">
                <fc:datePicker id="risks-changes-to-date" bs4="bs4" class="form-control form-control-sm dateControl" targetField="toDate.date"/>
            </div>
        </div>
%{--        Commenting this out as the PDF report needs work--}%
%{--        <div class="control-group">--}%
%{--            <label class="control-label">PDF Orientation: <fc:iconHelp>If your PDF includes activities with wide tables, the Landscape setting may improve the result.  This setting has no effect on the HTML view.</fc:iconHelp></label>--}%

%{--            <div class="controls">--}%
%{--                <select data-bind="value:orientation">--}%
%{--                    <option value="portrait">Portrait</option>--}%
%{--                    <option value="landscape">Landscape</option>--}%
%{--                </select>--}%
%{--            </div>--}%
%{--        </div>--}%
    </form>

    <form class="form-group">
        <button type="button" class="btn btn-sm btn-success" id="generateRisksReportHTML"
                data-bind="click:generateRisksReportHTML">Generate Report</button>
        %{--        Commenting this out as the PDF report needs work--}%
%{--        <button type="button" class="btn btn-sm btn-success"--}%
%{--                data-bind="click:generateRisksReportPDF">Generate Report (PDF)</button>--}%
    </form>

</div>
<!-- /ko -->
