function addProduct(){
    var product = $('#proSelect').data('select2:data');
    if(!product) return alert('请先选择要增加的授权产品');
    var proAutoIds = [];
    $('#batchBox input[name=proAutoId]').each(function(){
        proAutoIds.push($(this).formData());
    });
    if(proAutoIds.includes(product.autoId)) return alert('产品['+product.id+']已选');
    $('#batchBox').append(tpl_proAuth(product));
    widget_select2_init();
}

var BatchData, BatchUsers, BatchUserIdx, BatchUser, BatchProIdx, BatchAction;
var BatchRstIgUsers;
function batchBegin(){
    var data = $('#batchBox').formData();
    if(data.length==1) return alert('待授权的产品权限信息为空');
    if(!data) return;
    var userLines = Tarr.remove(data[0].users.split(/\n/), '');
    if(!userLines.length) return alert('用户信息为空');
    var users = [];
    for(var i=0; i<userLines.length; i++){
        var userLine = Tarr.remove(userLines[i].split(/,|，|\r|\n|\t| |　|;|；/), '');
        if(!userLine.length) continue;
        users.push({
            id : userLine[0]||null,
            name : userLine[1]||null,
            des : userLine.slice(2).join('\n')||null
        });
    }
    if(!users.length) return alert('用户信息为空');
    var batchAction = $('#batchAction').val();
    if(!confirm('确定要为'+users.length+'个用户在'+(data.length-1)+'个产品上执行'+batchAction+'的操作吗？')) return;
    
    //将收集上来的各产品权限信息由["1","2"]整数化为[1,2]
    for(var i=1; i<data.length; i++){
        var proAuthData = data[i];
        ['roleAutoIds','accessAutoIds','permissionAutoIds'].forEach(function(autoIdsKey){
            var ids = proAuthData[autoIdsKey];
            if(!ids) return;
            for(var j=0; j<ids.length; j++){
                ids[j] = Tint(ids[j]);
            }
        })
    }
    
    BatchData = data;
    BatchUsers = users;
    BatchUserIdx = 0;
    BatchAction = batchAction;
    
    BatchRstIgUsers = [];
    
    $('#batchAction').prop('disabled', true);
    $('#batchBtn').prop('disabled', true);
    $('#batchMsg').text('').show();
    
    batchUser();
}
function batchUser(){
    var user = BatchUsers[BatchUserIdx];
    if(!user){
        batchMsg('\n\n结束');
        $('#batchAction').prop('disabled', false);
        $('#batchBtn').prop('disabled', false);
        return;
    }
    batchMsg('\n第'+(BatchUserIdx+1)+'/'+BatchUsers.length+'个用户：'+user.id);
    
    batchMsg(' 检查是否注册：');
    q('/edit/user/get', {id:user.id}, function(userPo){
        if(userPo!=null){
            batchMsg('已注册');
            BatchUser = userPo;
            BatchProIdx = 1;
            batchPro();
            return;
        }
        batchMsg('未注册，创建中：');
        if(user.id && user.name){
            q('/edit/user/set', user, function(user){
                batchMsg('创建成功');
                BatchUser = user;
                BatchProIdx = 1;
                batchPro();
            })
        }else {
            batchMsg('用户信息不全，跳过。');
            BatchUserIdx += 1;
            batchUser();
        }
    });
}
function batchPro(){
    var proAuth = BatchData[BatchProIdx];
    if(!proAuth){
        BatchUserIdx += 1;
        return batchUser();
    }
    
    batchMsg('\n　第'+BatchProIdx+'/'+(BatchData.length-1)+'个产品权限:'+proAuth.proId+"：");
    var proAutoId = proAuth.proAutoId;
    
    batchMsg('载入权限数据：');
    q('/edit/user/get', {autoId:BatchUser.autoId,needAuth:true,proAutoId:proAutoId}, function(userWithAuth){
        var userAuth = userWithAuth2UserAuth(proAutoId, userWithAuth);
        
        batchMsg(BatchAction+'：');
        switch(BatchAction){
        case '添加权限': batchUserAddAuth(userAuth, proAuth); break;
        case '移除权限': batchUserDelAuth(userAuth, proAuth); break;
        }
        
        q('/edit/auth/user/set', userAuth, function(){
            batchMsg('成功');
            BatchProIdx += 1;
            batchPro();
        });
    });
    
}
function userWithAuth2UserAuth(proAutoId, userWithAuth){
    var userAuth = {};
    userAuth.proAutoId = proAutoId;
    userAuth.userAutoId = userWithAuth.autoId;
    if(userWithAuth.params && userWithAuth.params.length){
        userAuth.params = {};
        userWithAuth.params.forEach(function(param){
            if(!param.val) return;
            userAuth.params[param.autoId] = param.val;
        })
    }
    var models2AutoIds = {
        roles: 'roleAutoIds',
        accesses: 'accessAutoIds',
        permissions: 'permissionAutoIds'
    };
    for(var modelsKey in models2AutoIds){
        var models = userWithAuth[modelsKey];
        if(!models) continue;
        var autoIdsKey = models2AutoIds[modelsKey];
        userAuth[autoIdsKey] = [];
        models.forEach(function(model){
            userAuth[autoIdsKey].push(model.autoId);
        })
    }
    return userAuth;
}
function batchUserAddAuth(userAuth, authDelta){
    var autoIdsKeys = ['roleAutoIds', 'accessAutoIds', 'permissionAutoIds'];
    ['roleAutoIds', 'accessAutoIds', 'permissionAutoIds'].forEach(function(autoIdsKey){
        var deltaIds = authDelta[autoIdsKey];
        if(!deltaIds) return;
        var autoIds = userAuth[autoIdsKey];
        if(!autoIds) autoIds = userAuth[autoIdsKey] = [];
        deltaIds.forEach(function(autoId){
            if(autoIds.includes(autoId)) return;
            autoIds.push(autoId);
        })
    })
}
function batchUserDelAuth(userAuth, authDelta){
    var autoIdsKeys = ['roleAutoIds', 'accessAutoIds', 'permissionAutoIds'];
    ['roleAutoIds', 'accessAutoIds', 'permissionAutoIds'].forEach(function(autoIdsKey){
        var deltaIds = authDelta[autoIdsKey];
        if(!deltaIds) return;
        var autoIds = userAuth[autoIdsKey];
        if(!autoIds) return;
        deltaIds.forEach(function(autoId){
            if(!autoIds.includes(autoId)) return;
            autoIds = Tarr.remove(autoIds, autoId);
        })
        userAuth[autoIdsKey] = autoIds;
    })
}


function batchMsg(msg){
    $('#batchMsg').append(msg);
    window.scrollTo(0, document.body.scrollHeight);
}


var tpl_proAuth = $tpl(function(pro){
    if(!pro) return '';
    /*<div name data-type="obj" class="panel panel-default">
        <input name="proAutoId" data-type="int" type="hidden" value="{pro.autoId}"/>
        <input name="proId" type="hidden" value="{pro.id}"/>
        <div class="panel-heading">
            <h4 class="panel-title">
                <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#batchBox" href="#pro-{pro.autoId}">
                    <i class="ace-icon fa fa-angle-right bigger-110" data-icon-hide="ace-icon fa fa-angle-down" data-icon-show="ace-icon fa fa-angle-right"></i>
                    &nbsp;授权产品 {Tigh(pro.name)}({Tigh(pro.id)})
                    <i onclick="$(this).closest('.panel').remove()" class="ace-icon fa fa-close pull-right" style="cursor:pointer;"></i>
                </a>
            </h4>
        </div>
    
        <div id="pro-{pro.autoId}" class="panel-collapse collapse">
            <div class="panel-body form-horizontal">
                <div class="form-group">
                    <label class="col-xs-1 control-label">角色：</label>
                    <div class="col-xs-11">
                        <select name="roleAutoIds" proAutoId="{pro.autoId}" widget="select2" model="role" select2multiple="true" allow-clear="true" placeholder="请选择角色" style="width:100%"></select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-1 control-label">访问项：</label>
                    <div class="col-xs-11">
                        <select name="accessAutoIds" proAutoId="{pro.autoId}" widget="select2" model="access" select2multiple="true" allow-clear="true" placeholder="请选择访问项" style="width:100%"></select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-1 control-label">授权项：</label>
                    <div class="col-xs-11">
                        <select name="permissionAutoIds" proAutoId="{pro.autoId}" widget="select2" model="permission" select2multiple="true" allow-clear="true" placeholder="请选择授权项" style="width:100%"></select>
                    </div>
                </div>
            </div>
        </div>
    </div>*/
});