$(function(){
    loadClusterStats();
});

function loadClusterStats(){
    $("#clusterStatsBody").hide(); 
    q('/edit/cluster/stats', null, function(memberStats){
        $('#members').html(tpl_clusterStats(memberStats));
        $('#clients').html(tpl_clients(memberStats));
        $("#clusterStatsBody").show();
    }, '加载集群清单中');
}

function cacheClean(){
    var data = {
        proId : ($('#cacheCleanDiv [name=proId]').data('select2:data')||{}).id,
        userId : ($('#cacheCleanDiv [name=userId]').data('select2:data')||{}).id,
    };
    q('/edit/cache/clean', data, function(){
        alert('通知成功');
    }, '通知清理缓存中');
}

var tpl_clusterStats = $tpl(function(memberStats){
    memberStats.forEach(function(memberStatus){
        /*<tr><td title="{memberStatus.regTime?('注册时间'+TtimestampFormat(memberStatus.regTime)):''}">{Tigh(memberStatus.id)}</td><td>{Tigh(memberStatus.status)}</td><td>{Tigh((memberStatus.clients||[]).length)}</td></tr>*/
    })
});
var tpl_clients = $tpl(function(memberStats){
    var proClients = {};
    memberStats.forEach(function(memberStatus){
        if(!memberStatus.clients) return;
        memberStatus.clients.forEach(function(client){
            client.memberStatus = memberStatus;
            var clients = proClients[client.proId]||[];
            clients.push(client);
            proClients[client.proId] = clients;
        });
    });
    for(var proId in proClients){
        /*<tr><td><div style="width:max-content;">{Tigh(proId)}</div></td><td>*/
        proClients[proId].forEach(function(client){
            /*<span class="badge badge-{getBadge(client.host)} msg-tooltiper" style="cursor:default;">
                <span class="auth-name">{Tigh(client.host)}</span>
                <div class="tooltip-msg">
                    id:{Tigh(client.id)}<br>
                    {Tigh(client.fromHost)}:{Tigh(client.fromPort)}<br>
                    version:{Tigh(client.version)}<br>
                    注册时间:{Tigh(TtimestampFormat(client.regTime))}<br>
                    注册节点:{Tigh(client.memberStatus.address)}<br>
                </div>
            </span>*/
        })
        /*</td></tr>*/
    }
});