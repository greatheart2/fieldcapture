<div id="carousel" class="slider-pro row-fluid" data-bind="visible:mainImageUrl()" style="margin-bottom:20px;">
    <div class="sp-slides">
        <div class="sp-slide">
            <img class="sp-image" data-bind="attr:{'data-src':mainImageUrl}"/>
            <p class="sp-layer sp-white sp-padding"
               data-position="topLeft" data-width="100%" data-bind="visible:url"
               data-show-transition="down" data-show-delay="0" data-hide-transition="up">
                <strong data-bind="visible:url()">Visit us at <a data-bind="attr:{href:url}"><span data-bind="text:url"></span></a></strong>
            </p>
        </div>
    </div>
</div>

<div id="weburl" data-bind="visible:!mainImageUrl() && url()">
    <div data-bind="visible:url()"><strong>Visit us at <a data-bind="attr:{href:url}"><span data-bind="text:url"></span></a></strong></div>
</div>

<div data-bind="visible:description">
    <div class="well">
        <div class="well-title">Description</div>
        <div class="span8">
              <span data-bind="html:description.markdownToHtml()"></span>
        </div>
    </div>
</div>

<div data-bind="visible:programSiteId">
    <div class="span4">
        <m:map id="programSiteMap" style="width:100%;height:200px;"></m:map>
    </div>
</div>

<g:if test="${servicesDashboard.visible && servicesDashboard.services}">
    <hr/>
    <div class="well-title">Service delivery</div>
    <div id="services-dashboard">

        <g:if test="${servicesDashboard.planning}">
            <b>Please note this project is currently in a planning phase so delivery against the targets below has not yet begun</b>
        </g:if>
        <g:each in="${servicesDashboard.services}" var="service" status="i">

            <div class="dashboard-section" style="padding:10px; margin-top:10px;">
                <h3>${service.name}</h3>
                <g:each in="${service.scores}" var="score">
                    <fc:renderScore score="${score}"></fc:renderScore>
                </g:each>

            </div>

        </g:each>
    </div>
</g:if>

