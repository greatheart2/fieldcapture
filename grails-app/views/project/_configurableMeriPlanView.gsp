<div class="meri-plan space-after" data-bind="let:{details:meriPlan()}">

<g:each var="content" in="${config?.meriPlanContents}">
    <div class="row">
        <div class="col-sm-12 p-3">
            <g:render template="/project/meriPlanReadOnly/${content.template}" model="${content.model}"/>
        </div>
    </div>
</g:each>

</div>
