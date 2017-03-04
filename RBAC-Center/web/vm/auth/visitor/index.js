function loadVisitorAuth() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/auth/visitor/get.json',
        {
            proId: curPro.id,
        },
        function(visitorAuth){
            $('#params').html($tpl(tpl_visitorParams)(visitorAuth.params));
            $('#roles').html($tpl(tpl_roles)(visitorAuth.roles, true));
            $('#accesses').html($tpl(tpl_accesses)(visitorAuth.accesses, true));
            $('#permissions').html($tpl(tpl_permissions)(visitorAuth.permissions, true));
            $('#allAccesses').html($tpl(tpl_accesses)(visitorAuth.allAccesses, false));
            $('#allPermissions').html($tpl(tpl_permissions)(visitorAuth.allPermissions, false));
            
            $('#role').select2Clear();
            $('#access').select2Clear();
            $('#permission').select2Clear();
            $('#editVisitorAuthDiv').slideDown();
        },
        '加载游客权限中'
    );
}

function onRAPChange() {
    var visitorAuth = $('#editVisitorAuthDiv').formData();
    if(!visitorAuth) visitorAuth={};
    visitorAuth.proId = curPro.id;
    q('/edit/auth/visitor/cal.json',
        visitorAuth,
        function(visitorAuth){
            $('#allAccesses').html($tpl(tpl_accesses)(visitorAuth.allAccesses, false));
            $('#allPermissions').html($tpl(tpl_permissions)(visitorAuth.allPermissions, false));
        },
        '计算游客最终权限中'
    );
}

function editVisitorAuthSave() {
    var visitorAuth = $('#editVisitorAuthDiv').formData();
    if(!visitorAuth) visitorAuth={};
    visitorAuth.proId = curPro.id;
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