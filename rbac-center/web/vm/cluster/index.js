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
        /*<tr><td>{Tigh(memberStatus.address)}</td><td>{Tigh(memberStatus.status)}</td><td>{Tigh((memberStatus.clients||[]).length)}</td></tr>*/
    })
});
var tpl_clients = $tpl(function(memberStats){
    var proClients = {};
    memberStats.forEach(function(memberStatus){
        if(!memberStatus.clients) return;
        memberStatus.clients.forEach(function(client){
            var clients = proClients[client.proId]||[];
            clients.push(client);
            proClients[client.proId] = clients;
        });
    });
    for(var proId in proClients){
        /*<tr><td>{Tigh(proId)}</td><td>*/
        proClients[proId].forEach(function(client){
            /*<span class="badge badge-{getBadge(client.clientId)} msg-tooltiper" style="cursor:default;">
                <span class="auth-name">{Tigh(client.clientId)}</span>
                <div class="tooltip-msg">
                    {Tigh(client.host)}:{Tigh(client.port)}<br>
                    version:{Tigh(client.version)}
                </div>
            </span>*/
        })
        /*</td></tr>*/
    }
});