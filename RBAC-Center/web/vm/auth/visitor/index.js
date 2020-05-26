function loadVisitorAuth() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/auth/visitor/get.json',
        {
            proAutoId: curPro.autoId,
        },
        function(visitorAuth){
            $('#params').html($tpl(tpl_params)(visitorAuth.params));
            $('#roles').html($tpl(tpl_auths)(visitorAuth.roles, 1));
            $('#accesses').html($tpl(tpl_auths)(visitorAuth.accesses, 1));
            $('#permissions').html($tpl(tpl_auths)(visitorAuth.permissions, 1));
            onAuthChange();
        }, '加载游客权限中'
    );
}

function onAuthChange() {
    var visitorAuth = $('#editVisitorAuthDiv').formData();
    if(!visitorAuth) visitorAuth={};
    visitorAuth.proId = curPro.id;
    q('/edit/auth/visitor/cal.json',
        visitorAuth,
        function(visitorAuth){
            $('#allAccesses').html($tpl(tpl_allAuths)(visitorAuth.allAccesses));
            $('#allPermissions').html($tpl(tpl_allAuths)(visitorAuth.allPermissions));
            $('.a-auth-search').change();
        },
        '计算游客最终权限中'
    );
}

function editVisitorAuthSave() {
    var visitorAuth = $('#editVisitorAuthDiv').formData();
    if(!visitorAuth) visitorAuth={};
    visitorAuth.proAutoId = curPro.autoId;
    q('/edit/auth/visitor/set.json',
        visitorAuth,
        function() {
            alert('保存成功!');
        },
        '保存游客权限中'
    );
}

$(document).ready(ready);

function ready(){
    loadVisitorAuth();
}

function tpl_visitorParams(params) {
    if (!params) return;
    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        /*<div class="form-group">
            <label class="col-xs-3 control-label">
                <span title="{Tigh(param.key)}">{Tigh(param.name)}：</span>
            </label>
            <div class="col-xs-9 msg-tooltiper">
                <textarea name="{Tigh(param.key)}" class="form-control" placeholder="{Tigh(param.des)}">{Tigh(param.val)}</textarea>
                <div class="tooltip-msg">{Tigh(param.des).replace(/\n/g, '<br>')}</div>
            </div>
        </div>*/
    }
}
