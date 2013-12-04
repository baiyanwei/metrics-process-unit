package com.secpro.platform.monitoring.process.chains.ref.standard;
/**
 * 策略以及配置信息标准化所需常量
 * @author sxf
 *
 */
public class StandardConstant {
	//源IP地址名称
	public static final String SRC_IP_NAME="srcip";
	//目的IP地址名称
	public static final String DST_IP_NAME="dstip";
	//服务名称，包括：协议，源端口，目的端口
	public static final String SERVICE_NAME="service";
	//id名称，某些防火墙定义id字段，用于对防火墙策略进行排序
	public static final String ID_NAME="id";
	//ip地址
	public static final String IP_ADDRESS="ipAdd";
	//ip地址段
	public static final String RANGE_ADDRESS="rangeAdd";
	//子网
	public static final String NET_ADDRESS="netAdd";
	//地址段排除某些IP地址
	public static final String RANGE_ADDRESS_EXCEPT="rangeAddExc";
	//子网排除某些IP地址
	public static final String NET_ADDRESS_EXCEPT="netAddExc";
	//预定义服务
	public static final String PREDEFINED_SERVICE="preService";
	//定义子网时的子属性
	public static final String SUBNET="subNet";
	//定义子网时的子属性，子网掩码，支持/24格式，以及255。255。255.0
	public static final String SUBNET_MARK="mark";
	//地址段子属性，定义IP地址起始地址
	public static final String RANGE_START="ipS";
	//地址段子属性，定义IP地址终止地址
	public static final String RANGE_END="ipE";
	//子网地址排除子属性，定义排除地址
	public static final String SUBNET_EXCEPT="netExc";
	//地址范围排除子属性，定义排除地址
	public static final String RANGE_EXCEPT="rangeExc";
	//对标准化后的策略信息进行排序时定义，desc表示对标准化后的顺序进行倒序排列
	public static final String DESC="desc";
	//对标准化后的策略信息进行排序时定义，id:desc表示对id字段进行倒序排列，必须标准化id字段才可以设置
	public static final String ID_DESC="id:desc";
	//对标准化后的策略信息进行排序时定义，id:desc表示对id字段进行升序排列，必须标准化id字段才可以设置
	public static final String ID_ASC="id:asc";
	//icmp协议名称
	public static final String ICMP_PROTOCOL="icmpPro";
	//icmp协议子属性，协议类型
	public static final String ICMP_TYPE="icmpType";
	//icmp协议子属性，协议编码
	public static final String ICMP_CODE="icmpCode";
	//其他协议，如在策略中直接定义某一个协议
	public static final String OTHER_PROTOCOL="otherPro";
	//其他协议子属性，协议编码
	public static final String OTHER_PROTOCOL_NUM="proNum";
	//tcp、udp或其他定义有源端口，目的端口的协议
	public static final String TCP_UDP_PROTOCOL="tcpAndUdp";
	//tcp、udp协议子属性，协议名称
	public static final String TCP_UDP_PROTOCOL_NAME="tcpAndUdpName";
	//tcp、udp协议子属性，源端口起始
	public static final String TCP_UDP_SRC_PORT_START="srcPortS";
	//tcp、udp协议子属性，目的端口起始
	public static final String TCP_UDP_DST_PORT_START="dstPortS";
	//tcp、udp协议子属性，源端口终止
	public static final String TCP_UDP_SRC_PORT_END="srcPortE";
	//tcp、udp协议子属性，目的端口终止
	public static final String TCP_UDP_DST_PORT_END="dstPortE";
	//原始信息名称
	public static final String ORIGIN_NAME="origin";
	//当配置标准化规则配置此值时，将保存防火墙整个配置信息
	public static final String CONFIG_SAVE_ALL_VALUES="saveAll";
}
