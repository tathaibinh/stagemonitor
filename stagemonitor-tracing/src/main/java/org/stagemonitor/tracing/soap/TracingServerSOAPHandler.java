package org.stagemonitor.tracing.soap;

import com.uber.jaeger.context.TracingUtils;

import org.stagemonitor.tracing.TracingPlugin;
import org.stagemonitor.tracing.utils.SpanUtils;

import javax.xml.ws.handler.soap.SOAPMessageContext;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

/**
 * This class will be injected to implementations of {@link javax.xml.ws.Binding}
 *
 * <p> In JBoss for example this implementations is loaded by a module class loader which means we have one class which
 * is accessed by multiple Applications </p>
 */
public class TracingServerSOAPHandler extends AbstractTracingSOAPHandler {

	public TracingServerSOAPHandler() {
		super(true);
	}

	public TracingServerSOAPHandler(TracingPlugin tracingPlugin, SoapTracingPlugin soapTracingPlugin) {
		super(tracingPlugin, soapTracingPlugin, true);
	}

	@Override
	protected void handleInboundSOAPMessage(SOAPMessageContext context) {
		final Tracer.SpanBuilder spanBuilder = tracingPlugin.getTracer()
				.buildSpan(getOperationName(context))
				.withTag(SpanUtils.OPERATION_TYPE, "soap")
				.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
		if (soapTracingPlugin.isSoapServerRecordRequestMessages()) {
			spanBuilder.withTag("soap.request", getSoapMessageAsString(context));
		}
		if (!TracingUtils.getTraceContext().isEmpty()) {
			spanBuilder.asChildOf(TracingUtils.getTraceContext().getCurrentSpan());
		}
		spanBuilder.start();
	}

	@Override
	protected void handleOutboundSOAPMessage(SOAPMessageContext context) {
		if (soapTracingPlugin.isSoapServerRecordResponseMessages()) {
			if (!TracingUtils.getTraceContext().isEmpty()) {
				final Span span = TracingUtils.getTraceContext().getCurrentSpan();
				span.setTag("soap.response", getSoapMessageAsString(context));
			}
		}
	}

}
