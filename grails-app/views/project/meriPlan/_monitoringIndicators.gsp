<div class="meri-monitoring-indicators">
<table class="table">
    <thead>
    <tr>
        <th class="index"></th>
        <th class="baseline">Monitoring indicator<fc:iconHelp title="Monitoring indicator">List the indicators of project success that will be monitored. Add a new row for each indicator, e.g. ground cover condition, increased abundance of a particular species, increased engagement of community in delivery of on-ground works.</fc:iconHelp></th>
        <th class="baseline-method">Monitoring approach <fc:iconHelp title="Monitoring approach">How will this indicator be monitored? Briefly describe the method to be used to monitor the indicator.</fc:iconHelp></th>
        <th class="remove"></th>
    </tr>
    </thead>
    <tbody data-bind="foreach : details.objectives.rows">
    <tr>
        <td class="index"> <span data-bind="text:$index()+1"></span></td>
        <td class="baseline"> <textarea data-bind="value: data1, disable: $parent.isProjectDetailsLocked()" rows="3"> </textarea></td>
        <td class="baseline-method"> <textarea data-bind="value: data2, disable: $parent.isProjectDetailsLocked()" rows="5" ></textarea> </td>
        <td class="remove">
            <span data-bind="if: $index() && !$parent.isProjectDetailsLocked()"><i class="icon-remove" data-bind="click: $parent.removeObjectives"></i></span>
        </td>
    </tr>
    </tbody>
    <tfoot>
    <tr>
        <td colspan="5">
            <button type="button" class="btn btn-small" data-bind="disable:isProjectDetailsLocked(), click: addObjectives">
                <i class="icon-plus"></i> Add a row</button>
        </td>
    </tr>
    </tfoot>
</table>
</div>