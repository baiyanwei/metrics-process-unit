package com.secpro.platform.monitoring.process.http.watchdog;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.secpro.platform.api.server.IHttpRequestHandler;
import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.services.ProcessChainService;
/**
 * 接收watchdog发送的数据
 * @author sxf
 *
 */
public class WatchDogHttpRequestHandler implements IHttpRequestHandler {
	final private static PlatformLogger theLogger = PlatformLogger
			.getLogger(WatchDogHttpRequestHandler.class);
	private String id = null;
	private String name = null;
	private String description = null;
	@XmlElement(name = "path", type = String.class)
	public String path = "";

	@Override
	public Object DELETE(HttpRequest request, Object messageObj)
			throws Exception {
		return "DELETE";
	}

	@Override
	public Object HEAD(HttpRequest request, Object messageObj) throws Exception {
		return "HEAD";
	}

	@Override
	public Object OPTIONS(HttpRequest request, Object messageObj)
			throws Exception {
		return "OPTIONS";
	}

	@Override
	public Object PUT(HttpRequest request, Object messageObj) throws Exception {
		if (messageObj == null) {
			theLogger.debug("the messages are empty!");
			return "";
		}
		ProcessChainService processChainService = ServiceHelper
				.findService(ProcessChainService.class);
		if (processChainService == null) {
			throw new PlatformException("Can't find the ProcessChainService!");
		} else {
			processChainService.dataProcess(messageObj);
		}
		return "OK";
	}

	@Override
	public Object TRACE(HttpRequest request, Object messageObj)
			throws Exception {
		return "TRACE";
	}

	@Override
	public Object GET(HttpRequest request, Object messageObj) throws Exception {
		return POST(request, messageObj);
	}

	@Override
	public Object POST(HttpRequest request, Object messageObj) throws Exception {
		return "OK";
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getRequestMappingPath() {
		// TODO Auto-generated method stub
		return this.path;
	}

	public String toString() {
		return theLogger.MessageFormat("toString", name, path);
	}

	@Override
	public void fireSucceed(Object messageObj) throws PlatformException {
		// TODO Auto-generated method stub

	}

	@Override
	public void fireError(Object messageObj) throws PlatformException {
		// TODO Auto-generated method stub

	}

}