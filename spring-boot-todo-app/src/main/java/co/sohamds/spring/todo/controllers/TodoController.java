package co.sohamds.spring.todo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import co.sohamds.spring.todo.domain.Todo;
import co.sohamds.spring.todo.repository.TodoRepository;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@Controller
public class TodoController {
	OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
	Tracer tracer = openTelemetry.getTracer("tracer for todo app");
		
	@Autowired
	TodoRepository todoRepository;

	@GetMapping
	public String index() {
		return "index.html";
	}

	@GetMapping("/todos")
	public String todos(Model model) {
		model.addAttribute("todos", todoRepository.findAll());
		return "todos";
	}

	@PostMapping("/todoNew")
	public String add(@RequestParam String todoItem, @RequestParam String status, Model model) {
		Span span = tracer.spanBuilder("adding new task").startSpan();
		Todo todo = new Todo(todoItem, status);
		todo.setTodoItem(todoItem);
		todo.setCompleted(status);
		todoRepository.save(todo);
		model.addAttribute("todos", todoRepository.findAll());
		span.end();
		return "redirect:/todos";
	}

	@PostMapping("/todoDelete/{id}")
	public String delete(@PathVariable long id, Model model) {
		Span span = tracer.spanBuilder("deleting a task").startSpan();
		todoRepository.deleteById(id);
		model.addAttribute("todos", todoRepository.findAll());
		span.end();
		return "redirect:/todos";
	}

	@PostMapping("/todoUpdate/{id}")
	public String update(@PathVariable long id, Model model) {
		Span span = tracer.spanBuilder("updating").startSpan();
		Todo todo = todoRepository.findById(id).get();
		if ("Yes".equals(todo.getCompleted())) {
			todo.setCompleted("No");
		} else {
			todo.setCompleted("Yes");
		}
		todoRepository.save(todo);
		model.addAttribute("todos", todoRepository.findAll());
		span.end();
		return "redirect:/todos";
	}
}