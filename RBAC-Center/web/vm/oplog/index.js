$(function(){
    var time = new Date();
    $('#queryDiv [name=timeCeiling]').val(TtimestampFormat(time).substring(0,10)+' 23:59:59');
    time.setDate(time.getDate()-1);
    $('#queryDiv [name=timeFloor]').val(TtimestampFormat(time).substring(0,10)+' 00:00:00');
    queryOpLogs();
    
    $(window).resize(resetLogDataWidth);
});

var Query;

function queryOpLogs(){
    $('#pager').pagerPageNo(1);
    Query = $('#queryDiv').formData()||{};
    splitIds(Query, 'actions');
    splitIds(Query, 'operators');
    Query.ascOrDesc = $('#sort').val();
    loadOpLogs();
}

function loadOpLogs() {
    Query.pager = $('#pager').pagerSerialize();
    q('/edit/oplogs.json', Query, function(data){
            $('#pager').pagerCount(data.count);
            $('#oplogs').html($tpl(tpl_oplogs)(data.oplogs, data.pros));
            resetLogDataWidth();
            logsDataTidy($('#oplogs tr'));
        }, '加载操作日志中'
    );
}
function resetLogDataWidth(){
    var thead = $('#oplogs').siblings('thead');
    var width = (thead.width()-300)/2;
    thead.find('td').eq(2).width(width);
    thead.find('td').eq(3).width(width);
}

function addUser() {
    $('#editUserDiv').formData({});
    $('#editUserDiv').dialog({
        title:'新增用户',
        minWidth:1000,
        buttons:{'保存':editUserSave}
    })
}

function sortUser() {
    var autoIds = [];
    $('#users tr').each(function(){
        autoIds.push($(this).attrData().autoId);
    });
    if(autoIds.length==0) return alert('无排序内容!');
    q('/edit/user/sort.json',
        {
            autoIds: autoIds,
        },
        function(){
            alert('保存排序成功!');
        }, '保存排序中'
    );
}

function editUser(btn) {
    var user = $(btn).closest('tr').attrData();
    $('#editUserDiv').formData(user);
    $('#editUserDiv').dialog({
        title:'修改用户',
        minWidth:1000,
        buttons:{'保存':editUserSave}
    })
}

function editUserSave() {
    var user = $('#editUserDiv').formData();
    if(!user) return;
    q('/edit/user/set.json',
        user,
        function(){
            alert('保存成功!');
            loadUsers();
        }, '保存用户中'
    );
}

function delUser(btn) {
    var user = $(btn).closest('tr').attrData();
    if(!confirm('确定要删除用户\nID:'+user.id+'\n名称:'+user.name+'\n吗?')) return;
    q('/edit/user/del.json',
        {
            autoId:user.autoId
        },
        function(){
            alert('删除成功!');
            loadUsers();
        }, '删除用户中'
    );
}


function tpl_oplogs(oplogs, pros) {
    if(!oplogs) return;
    for (var i = 0; i < oplogs.length; i++) {
        var oplog = oplogs[i];
        var pro = pros[oplog.proAutoId];
        /*<tr>
            <td title="{(oplog.ip?'操作人IP：':'')+Tigh(oplog.ip)}">{Tigh(oplog.operator)}</td>
            <td>{tpl_auths([pro])}<br>{Tigh(oplog.action)}<br>{TtimestampFormat(oplog.time)}</td>
            <td style="padding:0;"><div class="logdata">{tpl_logdata(oplog.orig)}</div></td>
            <td style="padding:0;"><div><div class="logdata">{tpl_logdata(oplog.now)}</div></div></td>
        </tr>*/
    }
}
var tpl_logdata = $tpl(function(data){
    data = Tjson(data);
    if(!data) return '';
    if(data.id) {/*<div autoId="{Tigh(data.autoId)}" seq="{Tigh(data.seq)}">ID：{Tigh(data.id)}</div>*/}
    if(data.key) {/*<div autoId="{Tigh(data.autoId)}" seq="{Tigh(data.seq)}">key：{Tigh(data.key)}</div>*/}
    if(data.pattern) {/*<div autoId="{Tigh(data.autoId)}" seq="{Tigh(data.seq)}">pattern：{Tigh(data.pattern)}</div>*/}
    
    if(data.name) {/*<div title="{Tigh(data.des)}">名称：{Tigh(data.name)}</div>*/}
    
    if(data.params && data.params.length){
        /*<div class="flex-container">
            <div style="width:fit-content">配置项：</div>
            <div class="flex-flex1">*/
                        for(var i=0; i<data.params.length; i++){
                            var param = data.params[0];
                            /*<div>
                                <span title="{Tigh(param.key)}">{Tigh(param.name)}</span> :
                                <span title="{Tigh(param.des)}" style="padding:1px;">{Tigh(param.val)}</span>
                            </div>*/
                        }
                    /*
            </div>
        </div>*/
    }
    
    if(data.roles && data.roles.length){
        /*<div class="flex-container">
            <div style="width:fit-content">角色：</div>
            <div class="flex-flex1">{tpl_auths(data.roles, 0)}</div>
        </div>*/
    }
    
    if(data.accesses && data.accesses.length){
        /*<div class="flex-container">
            <div style="width:fit-content">访问项：</div>
            <div class="flex-flex1">{tpl_auths(data.accesses, 0)}</div>
        </div>*/
    }
    
    if(data.permissions && data.permissions.length){
        /*<div class="flex-container">
            <div style="width:fit-content">授权项：</div>
            <div class="flex-flex1">{tpl_auths(data.permissions, 0)}</div>
        </div>*/
    }
});
function logsDataTidy(trs){
    trs.each(function(){
        var tds = $(this).find('td');
        tds.eq(3).resizable({
            autoHide:true,
            minWidth:tds.eq(3).width(),
            maxWidth:tds.eq(3).width(),
            minHeight:100
        });
        tds.eq(3).resize(function(){
            var tr = $(this).closest('tr');
            tr.find('.logdata').css('max-height', $(this).height()+'px');
        });
    });
}