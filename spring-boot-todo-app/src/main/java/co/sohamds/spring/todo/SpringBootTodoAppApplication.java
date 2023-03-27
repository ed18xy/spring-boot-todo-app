package co.sohamds.spring.todo;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.qos.logback.core.util.Duration;
//import antlr.debug.Tracer;
import co.sohamds.spring.todo.domain.Todo;
import co.sohamds.spring.todo.repository.TodoRepository;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

@SpringBootApplication
public class SpringBootTodoAppApplication implements CommandLineRunner {
	@Autowired
	public TodoRepository todoRepository;

	public static void main(String[] args) {
		OpenTelemetry openTelemetry;
		GlobalOpenTelemetry.resetForTest();//to fix re run issue
			ZipkinSpanExporter exporter = ZipkinSpanExporter.builder()
				.setEndpoint("http://localhost:9411/api/v2/spans")
				.build();
				
			SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
					.addSpanProcessor(SimpleSpanProcessor.create(exporter))
					.build();
					
			openTelemetry = OpenTelemetrySdk.builder()
					.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.setTracerProvider(tracerProvider)//.build();
				.buildAndRegisterGlobal();
			openTelemetry = GlobalOpenTelemetry.get();

		Tracer tracer = openTelemetry.getTracer("tracer for todo app");
		Span span = tracer.spanBuilder("running todo app")
				.startSpan();

		try {
			span.addEvent("Starting todo appliacation");
			SpringApplication.run(SpringBootTodoAppApplication.class, args);
		} catch (Exception e) {
			span.recordException(e);

		} finally {
			span.end();
		}
		// SpringApplication.run(SpringBootTodoAppApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {

		Todo test = Todo.builder().id(10).completed("its completed").todoItem("python ML").build();
		System.out.println(test.toString());
		List<Todo> todos = Arrays.asList(new Todo("Learn Spring", "Yes"), new Todo("Learn Driving", "No"),
				new Todo("Go for a Walk", "No"), new Todo("Cook Dinner", "Yes"));
		todos.forEach(todoRepository::save);

	}
}
