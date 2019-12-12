package com.lvt4j.rbac;

import static com.lvt4j.rbac.ProductAuth4Client.CacheCapacityDef;
import static com.lvt4j.rbac.ProductAuth4Client.RbacCenterAddrDef;
import static com.lvt4j.rbac.ProductAuth4Client.RbacCenterProtocolDef;
import static com.lvt4j.rbac.ProductAuth4Client.RbacCenterSyncIntervalDef;
import static com.lvt4j.rbac.ProductAuth4Client.RbacCenterSyncTimeoutDef;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 权限拦截器，配置参数:<br>
 * 1.proId(必须):在授权中心注册的产品ID<br>
 * 2.cacheCapacity(非必须):最大为多少用户缓存权限,默认{@link com.lvt4j.rbac.RbacBaseFilter#CacheCapacityDef 1000个}<br>
 * 3.rbacCenterProtocol(非必须):与授权中心同步的协议,http/https,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterProtocolDef http}<br>
 * 4.rbacCenterAddr(非必须):授权中心服务地址,[host](:[port])形式,默认{@link com.lvt4j.rbac.RbacBaseFilter#RbacCenterAddrDef 127.0.0.1:80}<br>
 * 5.rbacCenterSyncInterval(非必须):与授权中心服务同步时间间隔,单位分钟,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncIntervalDef 5分钟}<br>
 * 6.rbacCenterSyncTimeout(非必须):与授权中心同步超时时间,单位毫秒,默认{@link com.lvt4j.rbac.RbacBaseFilter.RbacCenterSyncTimeoutDef 200ms}<br>
 * 权限验证处理流程图:<br>
 * <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" id="processonSvg1000" viewBox="198.5 1049.0 515.5 568.0" width="515.5" height="568.0"><defs id="ProcessOnDefs1001"><marker id="ProcessOnMarker1019" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1020" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1027" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1028" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1039" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1040" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1043" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1044" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1047" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1048" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1055" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1056" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1059" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1060" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1091" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1092" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1103" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1104" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1111" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1112" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1119" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1120" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker><marker id="ProcessOnMarker1128" markerUnits="userSpaceOnUse" orient="auto" markerWidth="16.23606797749979" markerHeight="10.550836550532098" viewBox="-1.0 -1.3763819204711736 16.23606797749979 10.550836550532098" refX="-1.0" refY="3.8990363547948754"><path id="ProcessOnPath1129" d="M12.0 3.8990363547948754L0.0 7.798072709589751V0.0Z" stroke="#323232" stroke-width="2.0" fill="#323232" transform="matrix(1.0,0.0,0.0,1.0,0.0,0.0)"/></marker></defs><g id="ProcessOnG1002"><path id="ProcessOnPath1003" d="M198.5 1049.0H714.0V1617.0H198.5V1049.0Z" fill="none"/><g id="ProcessOnG1004"><g id="ProcessOnG1005" transform="matrix(1.0,0.0,0.0,1.0,247.0,1069.0)" opacity="1.0"><path id="ProcessOnPath1006" d="M16.666666666666668 0.0L83.33333333333333 0.0C105.55555555555556 0.0 105.55555555555556 50.0 83.33333333333333 50.0L16.666666666666668 50.0C-5.555555555555556 50.0 -5.555555555555556 0.0 16.666666666666668 0.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1007" transform="matrix(1.0,0.0,0.0,1.0,10.0,17.5)"><text id="ProcessOnText1008" fill="#000000" font-size="12" x="39.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">开始</text></g></g><g id="ProcessOnG1009" transform="matrix(1.0,0.0,0.0,1.0,247.0,1547.0)" opacity="1.0"><path id="ProcessOnPath1010" d="M16.666666666666668 0.0L83.33333333333333 0.0C105.55555555555556 0.0 105.55555555555556 50.0 83.33333333333333 50.0L16.666666666666668 50.0C-5.555555555555556 50.0 -5.555555555555556 0.0 16.666666666666668 0.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1011" transform="matrix(1.0,0.0,0.0,1.0,10.0,17.5)"><text id="ProcessOnText1012" fill="#000000" font-size="12" x="39.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">验证成功</text></g></g><g id="ProcessOnG1013" transform="matrix(1.0,0.0,0.0,1.0,594.0,1547.0)" opacity="1.0"><path id="ProcessOnPath1014" d="M16.666666666666668 0.0L83.33333333333333 0.0C105.55555555555556 0.0 105.55555555555556 50.0 83.33333333333333 50.0L16.666666666666668 50.0C-5.555555555555556 50.0 -5.555555555555556 0.0 16.666666666666668 0.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1015" transform="matrix(1.0,0.0,0.0,1.0,10.0,17.5)"><text id="ProcessOnText1016" fill="#000000" font-size="12" x="39.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">验证失败</text></g></g><g id="ProcessOnG1017"><path id="ProcessOnPath1018" d="M297.0 1119.0L297.0 1131.5L297.0 1131.5L297.0 1132.5" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1019)"/></g><g id="ProcessOnG1021" transform="matrix(1.0,0.0,0.0,1.0,470.0,1237.5)" opacity="1.0"><path id="ProcessOnPath1022" d="M0.0 22.0L62.0 0.0L124.0 22.0L62.0 44.0L0.0 22.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1023" transform="matrix(1.0,0.0,0.0,1.0,10.0,14.5)"><text id="ProcessOnText1024" fill="#000000" font-size="12" x="51.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">onNotLogin</text></g></g><g id="ProcessOnG1025"><path id="ProcessOnPath1026" d="M594.0 1259.5L644.0 1259.5L644.0 1531.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1027)"/></g><g id="ProcessOnG1029" transform="matrix(1.0,0.0,0.0,1.0,468.25,1349.75)" opacity="1.0"><path id="ProcessOnPath1030" d="M0.0 28.25L63.75 0.0L127.5 28.25L63.75 56.5L0.0 28.25Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1031" transform="matrix(1.0,0.0,0.0,1.0,10.0,20.75)"><text id="ProcessOnText1032" fill="#000000" font-size="12" x="52.75" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">onNotRegister</text></g></g><g id="ProcessOnG1033" transform="matrix(1.0,0.0,0.0,1.0,218.5,1348.0)" opacity="1.0"><path id="ProcessOnPath1034" d="M0.0 30.0L78.5 0.0L157.0 30.0L78.5 60.0L0.0 30.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1035" transform="matrix(1.0,0.0,0.0,1.0,10.0,22.5)"><text id="ProcessOnText1036" fill="#000000" font-size="12" x="67.5" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">已在权限中心注册?</text></g></g><g id="ProcessOnG1037"><path id="ProcessOnPath1038" d="M532.0 1281.5L532.0 1315.625L532.0 1315.625L532.0 1334.5139320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1039)"/></g><g id="ProcessOnG1041"><path id="ProcessOnPath1042" d="M297.0 1408.0L297.0 1432.0L297.0 1432.0L297.0 1440.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1043)"/></g><g id="ProcessOnG1045"><path id="ProcessOnPath1046" d="M375.5 1378.0L421.875 1378.0L421.875 1378.0L453.0139320225002 1378.0" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1047)"/></g><g id="ProcessOnG1049" transform="matrix(1.0,0.0,0.0,1.0,252.0,1233.0)" opacity="1.0"><path id="ProcessOnPath1050" d="M0.0 26.5L45.0 0.0L90.0 26.5L45.0 53.0L0.0 26.5Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1051" transform="matrix(1.0,0.0,0.0,1.0,10.0,19.0)"><text id="ProcessOnText1052" fill="#000000" font-size="12" x="34.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">已登陆?</text></g></g><g id="ProcessOnG1053"><path id="ProcessOnPath1054" d="M342.0 1259.5L406.0 1259.5L406.0 1259.5L454.7639320225002 1259.5" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1055)"/></g><g id="ProcessOnG1057"><path id="ProcessOnPath1058" d="M297.0 1286.0L297.0 1317.0L297.0 1317.0L297.0 1332.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1059)"/></g><g id="ProcessOnG1061" transform="matrix(1.0,0.0,0.0,1.0,342.0,1239.0)" opacity="1.0"><path id="ProcessOnPath1062" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1063" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1064" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">否</text></g></g><g id="ProcessOnG1065" transform="matrix(1.0,0.0,0.0,1.0,594.0,1239.0)" opacity="1.0"><path id="ProcessOnPath1066" d="M0.0 0.0L33.0 0.0L33.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1067" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1068" fill="#000000" font-size="12" x="15.5" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">false</text></g></g><g id="ProcessOnG1069" transform="matrix(1.0,0.0,0.0,1.0,299.0,1506.0)" opacity="1.0"><path id="ProcessOnPath1070" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1071" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1072" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">是</text></g></g><g id="ProcessOnG1073" transform="matrix(1.0,0.0,0.0,1.0,532.0,1281.5)" opacity="1.0"><path id="ProcessOnPath1074" d="M0.0 0.0L34.0 0.0L34.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1075" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1076" fill="#000000" font-size="12" x="16.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">true</text></g></g><g id="ProcessOnG1077" transform="matrix(1.0,0.0,0.0,1.0,299.0,1286.0)" opacity="1.0"><path id="ProcessOnPath1078" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1079" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1080" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">是</text></g></g><g id="ProcessOnG1081" transform="matrix(1.0,0.0,0.0,1.0,387.5,1356.0)" opacity="1.0"><path id="ProcessOnPath1082" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1083" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1084" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">否</text></g></g><g id="ProcessOnG1085" transform="matrix(1.0,0.0,0.0,1.0,299.0,1408.0)" opacity="1.0"><path id="ProcessOnPath1086" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1087" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1088" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">是</text></g></g><g id="ProcessOnG1089"><path id="ProcessOnPath1090" d="M595.75 1378.0L644.0 1378.0L644.0 1531.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1091)"/></g><g id="ProcessOnG1093" transform="matrix(1.0,0.0,0.0,1.0,594.0,1356.0)" opacity="1.0"><path id="ProcessOnPath1094" d="M0.0 0.0L33.0 0.0L33.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1095" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1096" fill="#000000" font-size="12" x="15.5" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">false</text></g></g><g id="ProcessOnG1097" transform="matrix(1.0,0.0,0.0,1.0,240.0,1456.0)" opacity="1.0"><path id="ProcessOnPath1098" d="M0.0 25.0L57.0 0.0L114.0 25.0L57.0 50.0L0.0 25.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1099" transform="matrix(1.0,0.0,0.0,1.0,10.0,17.5)"><text id="ProcessOnText1100" fill="#000000" font-size="12" x="46.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">访问项允许?</text></g></g><g id="ProcessOnG1101"><path id="ProcessOnPath1102" d="M532.0 1406.25L532.0 1431.125L297.0 1431.125L297.0 1440.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1103)"/></g><g id="ProcessOnG1105" transform="matrix(1.0,0.0,0.0,1.0,532.0,1408.0)" opacity="1.0"><path id="ProcessOnPath1106" d="M0.0 0.0L34.0 0.0L34.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1107" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1108" fill="#000000" font-size="12" x="16.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">true</text></g></g><g id="ProcessOnG1109"><path id="ProcessOnPath1110" d="M354.0 1481.0L644.0 1481.0L644.0 1531.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1111)"/></g><g id="ProcessOnG1113" transform="matrix(1.0,0.0,0.0,1.0,354.0,1461.0)" opacity="1.0"><path id="ProcessOnPath1114" d="M0.0 0.0L20.0 0.0L20.0 20.0L0.0 20.0Z" stroke="none" stroke-width="0.0" stroke-dasharray="none" opacity="1.0" fill="none"/><g id="ProcessOnG1115" transform="matrix(1.0,0.0,0.0,1.0,0.0,2.5)"><text id="ProcessOnText1116" fill="#000000" font-size="12" x="9.0" y="12.3" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="12">否</text></g></g><g id="ProcessOnG1117"><path id="ProcessOnPath1118" d="M297.0 1506.0L297.0 1526.5L297.0 1526.5L297.0 1531.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1119)"/></g><g id="ProcessOnG1121" transform="matrix(1.0,0.0,0.0,1.0,229.0,1144.0)" opacity="1.0"><path id="ProcessOnPath1122" d="M0.0 0.0L136.0 0.0L136.0 54.0L0.0 54.0Z" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" opacity="1.0" fill="#ffffff"/><g id="ProcessOnG1123" transform="matrix(1.0,0.0,0.0,1.0,10.0,10.75)"><text id="ProcessOnText1124" fill="#000000" font-size="13" x="57.0" y="13.325" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="13">提取并向request中</text><text id="ProcessOnText1125" fill="#000000" font-size="13" x="57.0" y="29.575" font-family="微软雅黑" font-weight="normal" font-style="normal" text-decoration="none" family="微软雅黑" text-anchor="middle" size="13">写入用户权限信息</text></g></g><g id="ProcessOnG1126"><path id="ProcessOnPath1127" d="M297.0 1198.0L297.0 1215.5L297.0 1215.5L297.0 1217.7639320225003" stroke="#323232" stroke-width="2.0" stroke-dasharray="none" fill="none" marker-end="url(#ProcessOnMarker1128)"/></g></g></g></svg>
 * @author LV
 */
public abstract class RbacFilter extends RbacBaseFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        String proId = config.getInitParameter("proId");
        if(strIsEmpty(proId)) throw new IllegalArgumentException("产品ID必须配置!");
        String cacheCapacityStr = config.getInitParameter("cacheCapacity");
        int cacehCapacity = strIsEmpty(cacheCapacityStr)?CacheCapacityDef:Integer.parseInt(cacheCapacityStr);
        String rbacCenterProtocol = config.getInitParameter("rbacCenterProtocol");
        rbacCenterProtocol = strIsEmpty(rbacCenterProtocol)?RbacCenterProtocolDef:rbacCenterProtocol;
        String rbacCenterAddr = config.getInitParameter("rbacCenterAddr");
        rbacCenterAddr = strIsEmpty(rbacCenterAddr)?RbacCenterAddrDef:rbacCenterAddr;
        String rbacCenterSyncIntervalStr = config.getInitParameter("rbacCenterSyncInterval");
        int rbacCenterSyncInterval = strIsEmpty(rbacCenterSyncIntervalStr)?RbacCenterSyncIntervalDef:Integer.parseInt(rbacCenterSyncIntervalStr);
        String rbacCenterSyncTimeoutStr = config.getInitParameter("rbacCenterSyncTimeout");
        int rbacCenterSyncTimeout = strIsEmpty(rbacCenterSyncTimeoutStr)?RbacCenterSyncTimeoutDef:Integer.parseInt(rbacCenterSyncTimeoutStr);
        productAuth = new ProductAuth4Client(proId, cacehCapacity,
                rbacCenterProtocol, rbacCenterAddr, rbacCenterSyncInterval, rbacCenterSyncTimeout);
    }

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse,
            FilterChain chain) throws IOException, ServletException{
        if(!(rawRequest instanceof HttpServletRequest) //非标准请求,忽略权限验证
                || !(rawResponse instanceof HttpServletResponse)){
            chain.doFilter(rawRequest, rawResponse);
            return;
        }
        HttpServletRequest request = (HttpServletRequest) rawRequest;
        HttpServletResponse response = (HttpServletResponse) rawResponse;
        String userId = getUserId(request, response);
        UserAuth userAuth = productAuth.getUserAuth(userId);
        request.setAttribute(UserAuth.ReqAttr, userAuth);
        if(strIsEmpty(userId) && !onNotLogin(request, response)) return;
        if(!userAuth.exist && !onNotRegister(request, response)) return;
        String uri = request.getRequestURI();
        if(!productAuth.allowAccess(userId, uri)
                && !onNotAllowAccess(request, response, userId, uri)) return;
        chain.doFilter(rawRequest, rawResponse);
    }

    @Override
    public void destroy() {
        productAuth.destory();
    }

}
