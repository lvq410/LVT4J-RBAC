$(loadVisitorAuth);

function loadVisitorAuth() {
    if(!curPro) return alert('请先选择当前产品!');
    q('/edit/auth/visitor/get.json',
        {
            proAutoId: curPro.autoId,
        },
        function(visitorAuth){
            $('#params').html(tpl_params(visitorAuth.params));
            $('#roles').html(tpl_auths(visitorAuth.roles, 1));
            $('#accesses').html(tpl_auths(visitorAuth.accesses, 1));
            $('#permissions').html(tpl_auths(visitorAuth.permissions, 1));
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
            $('#allAccesses').html(tpl_allAuths(visitorAuth.allAccesses));
            $('#allPermissions').html(tpl_allAuths(visitorAuth.allPermissions));
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