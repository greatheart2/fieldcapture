<!-- ko stopBinding: true -->
<h3>MERI Plan</h3>
<div id="edit-meri-plan">
<script id="submittedPlanTmpl" type="text/html">
<div class="required">
	<div class="form-actions" >
		<b>Grant manager actions:</b>
		<div data-bind="if:!canApprove()">
		<div class="alert alert-info">
			Insert the allocated Internal Order number for this project. If the Internal Order number is not available, insert ‘TBA’ as a placeholder to allow you to complete the MERI plan. The Internal Order number must be inserted as soon as it becomes available
		</div>
		<div>
			<label for="internalOrderId">Internal order number:</label>
			<input id="internalOrderId" type="text" placeholder="If unavailable, use 'TBA'" data-bind="value:internalOrderId, valueUpdate:'keyup'">
		</div>
		</div>
		<span class="grantManagerActionSpan">
			<button type="button" data-bind="enable:canApprove() || internalOrderId(), click:approvePlan" class="btn btn-sm btn-success"><i class="fa fa-check icon-white"></i> Approve</button>
			<button type="button" data-bind="click:rejectPlan" class="btn btn-sm btn-danger"><i class="fa fa-remove icon-white"></i> Reject</button>
		</span>
	</div>
</div>
</script>
<script id="approvedPlanTmpl" type="text/html">
<div class="required">
	<div class="form-actions">
		<b>Grant manager actions:</b>
		<button type="button" data-bind="click: modifyPlan"  id="modify-plan" class="btn btn-sm btn-info">Modify MERI Plan</button>
		<br/><br/>
		<ul>
			<li>"Modify MERI Plan" will allow project admin's to edit MERI plan information. </li>
			<li>Modifying the MERI plan will change the state of the project to "Not approved".</li>
		</ul>
	</div>
</div>
</script>
<script id="editablePlanTmpl">

</script>
<script id="completedProjectTmpl" type="text/html">
<div class="required">
	%{--<div class="form-actions" >--}%
		%{--<b>Grant manager actions:</b>--}%
		%{--<span class="btn-group">--}%
			%{--<button type="button" data-bind="click:unlockPlanForCorrection" class="btn btn-danger"><i class="fa fa-unlock"></i> Unlock plan for correction</button>--}%
		%{--</span>--}%
	%{--</div>--}%
</div>
</script>
<script id="unlockedProjectTmpl" type="text/html">
<div class="required">
	<div class="form-actions" >
		<b>Grant manager actions:</b>
		<span class="unlockGroup">
			<button type="button" data-bind="click:finishCorrections" class="btn btn-sm btn-success"><i class="fa fa-lock icon-white"></i> Finished corrections</button>
		</span>
	</div>
</div>
</script>

<g:render template="/shared/declaration" model="[divId:'unlockPlan', declarationType:au.org.ala.merit.SettingPageType.UNLOCK_PLAN_DECLARATION]"/>
<g:render template="meriPlanApprovalModal"/>
<div class="row mb-4">
	<div class="col-sm-6">
		<div class="control-group">
			<span class="badge text-white" style="font-size: 13px;" data-bind="text:meriPlanStatus().text, css:meriPlanStatus().badgeClass"></span>
			<span data-bind="if:detailsLastUpdated"> <br/>Last update date : <span data-bind="text:detailsLastUpdated.formattedDate"></span></span>
		</div>
	</div>
<g:if test="${showMeriPlanHistory}">
	<div class="col-sm-6">
		<div class="pull-right"><a class="btn btn-info btn-sm meri-history-toggle" data-bind="click:toggleMeriPlanHistory, text:meriPlanHistoryVisible() ? 'Hide approval history' : 'Show approval history'">Show MERI plan approvals</a></div>
	</div>
</g:if>

</div>

<g:if test="${showMeriPlanHistory}">
<div data-bind="visible:meriPlanHistoryVisible">
	<h4>History of approved MERI plans</h4>
	<div data-bind="visible:!meriPlanHistoryInitialised()">
		<asset:image src="spinner.gif"/>
	</div>
	<div data-bind="visible:meriPlanHistoryInitialised()">

	<div data-bind="visible:approvedPlans().length > 0">

	<table class="table table-striped meri-approval-history">
		<thead>
		<tr>
			<th class="approval-date">Date / Time Approved</th>
			<th class="ref">Change Order Numbers</th>
			<th class="comments">Comments</th>
			<th class="approver">Approved by</th>
			<th class="open">Open</th>
			<g:if test="${fc.userIsAlaOrFcAdmin()}">
				<th class="delete-approval">Delete</th>
			</g:if>
		</tr>
		</thead>
		<tbody data-bind="foreach:approvedPlans">
		<tr>
			<td class="approval-date" data-bind="text:dateApproved"></td>
			<td class="ref" data-bind="text:referenceDocument"></td>
			<td class="comments" data-bind="text:reason"></td>
			<td class="approver"><span data-bind="text:userDisplayName"></span></td>
			<td class="open"><a target="_meriPlan" data-bind="attr:{href:openMeriPlanUrl}"><i class="fa fa-external-link"></i></a></td>
			<g:if test="${fc.userIsAlaOrFcAdmin()}">
				<td class="delete-approval"><i class="fa fa-remove" data-bind="click:$parent.deleteApproval"></i></td>
			</g:if>
		</tr>
		</tbody>
	</table>
	</div>
	<div data-bind="visible:approvedPlans().length == 0">
		No MERI plan approvals found.
	</div>
	</div>
</div>
</g:if>


<!--  Case manager actions -->
<g:if test="${user?.isCaseManager}">
	<div class="row space-after">
		<div data-bind="template:meriGrantManagerActionsTemplate" class="col-sm-12"></div>
	</div>
</g:if>

<g:if test="${projectContent.details.visible}">
	<div class="save-details-result-placeholder"></div>
	<div class="row space-after">
		<div class="col-sm-12">
			<div class="form-actions">
				<div class="form-check">
					<input type="checkbox" class="form-check-input" id="caseStudy" data-bind="checked: meriPlan().caseStudy, disable: isProjectDetailsLocked()">
					<label for="caseStudy" class="form-check-label">&nbsp;Are you willing for your project to be used as a case study by the Department?</label>
				</div>
				<br/>
				<button type="button" data-bind="click: saveProjectDetails, disable: isProjectDetailsLocked()" class="btn btn-sm btn-primary">Save changes</button>
				<button type="button" class="btn btn-sm btn-danger" data-bind="click: cancelProjectDetailsEdits">Cancel</button>
				<button type="button" class="btn btn-sm btn-info" data-bind="click: meriPlanPDF">Generate PDF</button>

				<!--  Admin - submit to approval. -->
				<g:if test="${user?.isAdmin}">
				<div>
					<div data-bind="if: !isSubmittedOrApproved()">
						<hr/>
						<b>Admin actions:</b>
						<g:if test="${showMERIActivityWarning}">
						<ul>
							<li>You will not be able to report activity data until your MERI plan has been approved by your case manager.</li>
						</ul>
						</g:if>
						<g:if test="${allowMeriPlanUpload}">
							<div class="btn fileinput-button"
								 data-bind="fileUploadNoImage:meriPlanUploadConfig"><i class="fa fa-plus"></i> <input
									type="file" name="meriPlan"><span>Upload MERI Plan</span></div>
						</g:if>
						<button type="button" data-bind="click: saveAndSubmitChanges" class="btn btn-sm btn-info saveAndSubmitChanges">Submit for approval</button>
					</div>
					<div data-bind="if: isSubmittedOrApproved()">
                        <g:if test="${showMERIActivityWarning}">
						<hr/>

						<b>Admin:</b>
						<ul>
							<li>You will not be able to report activity data until your MERI plan has been approved by your case manager.</li>
						</ul>
						</g:if>
					</div>
				</div>
				</g:if>
			</div>

		</div>
	</div>

	<div class="controls">
		<b>From: </b><span data-bind="text: plannedStartDate.formattedDate"></span>  <b>To: </b> <span data-bind="text: plannedEndDate.formattedDate"></span>
	</div>

	<g:render template="${meriPlanTemplate}" model="${[config:config]}"/>

</g:if>

<div class="save-details-result-placeholder"></div>

<div class="row space-after">
	<div class="col-sm-12">
		<div class="form-actions">
			<div class="form-check">
				<input class="form-check-input" id="caseStudy2" type="checkbox"  data-bind="checked: meriPlan().caseStudy, disable: isProjectDetailsLocked()" />
				<label for="caseStudy2" class="form-check-label">&nbsp;Are you willing for your project to be used as a case study by the Department?</label>
			</div>
			<br/>

			<button type="button" data-bind="click: saveProjectDetails, disable: isProjectDetailsLocked()" class="btn btn-sm btn-primary">Save changes</button>
			<button type="button" class="btn btn-sm btn-danger" data-bind="click: cancelProjectDetailsEdits">Cancel</button>
			<g:if test="${projectContent.details.visible}"><button type="button" class="btn btn-sm btn-info" data-bind="click: meriPlanPDF">Generate PDF</button></g:if>

			<!--  Admin - submit to approval. -->
			<g:if test="${user?.isAdmin}">
			<div>
				<div data-bind="if:!isSubmittedOrApproved()">
					<hr/>
					<b>Admin actions:</b>
					<g:if test="${showActivityWarning}">
					<ul>
						<li>You will not be able to report activity data until your MERI plan has been approved by your grant manager.</li>
					</ul>
					</g:if>
					<g:if test="${allowMeriPlanUpload}">
						<div class="btn fileinput-button"
							 data-bind="fileUploadNoImage:meriPlanUploadConfig"><i class="icon-plus"></i>
							<input type="file" name="meriPlan"><span>Upload MERI Plan</span>
						</div>
					</g:if>
					<button type="button" data-bind="click: saveAndSubmitChanges" class="btn btn-sm btn-info saveAndSubmitChanges">Submit for approval</button>
				</div>
				<div data-bind="if: isSubmittedOrApproved()">
                    <g:if test="${showMERIActivityWarning}">
                    <hr/>
					<b>Admin:</b>
					<ul>
						<li>You will not be able to report activity data until your MERI plan has been approved by your grant manager.</li>
					</ul>
					</g:if>
				</div>
			</div>
			</g:if>
		</div>

	</div>
</div>

<div id="floating-save" style="display:none;">
	<div class="transparent-background"></div>
	<div><button class="right btn btn-sm btn-info" data-bind="click: saveProjectDetails">Save changes</button></div>
</div>
</div>
<!-- /ko -->
