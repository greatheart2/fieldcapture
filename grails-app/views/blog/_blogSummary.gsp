<!-- ko stopBinding:true -->
<div id="site-blog">
    <g:if test="${blog.size() > 0}">
        <ul class="unstyled list-unstyled" data-bind="foreach:entries">
            <li>
                <img data-bind="visible:imageUrl(), attr:{src:imageThumbnailUrl}" class="pull-left" width="50" height="50">
                <i class="blog-icon floatleft fa fa-3x" data-bind="visible:stockIcon(), css:stockIcon"></i>
                <div>
                    <div class="row">
                        <strong data-bind="text:title"></strong>
                        <div class="pull-right">
                            <a class="editThisBlog" href data-bind="click:$parent.editBlogEntry">Edit</a> |
                            <a class="delThisBlog" href data-bind="click:$parent.deleteBlogEntry">Delete</a>
                        </div>
                    </div>
                    <p data-bind="text:shortContent"></p>
                </div>
                <hr/>
            </li>

        </ul>
    </g:if>
    <g:else>
        No blog entries.
    </g:else>

    <div class="form-actions">
        <button data-bind="click:newBlogEntry" type="button" id="new" class="btn btn-sm btn-primary">New Entry</button>
    </div>
</div>
<!-- /ko -->

<asset:script>

$(function(){

    var blog = <fc:modelAsJavascript model="${blog}" default="[]"/>;
    ko.applyBindings(new BlogSummary(blog), document.getElementById('site-blog'));
});

</asset:script>
