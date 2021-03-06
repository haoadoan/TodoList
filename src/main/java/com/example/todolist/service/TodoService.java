package com.example.todolist.service;

import com.example.todolist.dto.ChangeOrderDto;
import com.example.todolist.dto.StatisticDto;
import com.example.todolist.dto.TodoDto;
import com.example.todolist.mapper.TodoMapper;
import com.example.todolist.models.Statistic;
import com.example.todolist.models.Todo;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.util.Calculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

  @Autowired
  private TodoMapper todoMapper;

  @Autowired
  private TodoRepository todoRepository;

  public List<Todo> getTodos(String status) {
    if ("completed".equalsIgnoreCase(status)) {
      return this.todoRepository.findAllByCompletedOrderByOrderAsc(true);
    }
    if ("active".equalsIgnoreCase(status)) {
      return this.todoRepository.findAllByCompletedOrderByOrderAsc(false);
    }
    return this.todoRepository.findAllByOrderByOrderAsc();
  }

  public Todo addTodo(TodoDto todoDto) {
    Todo todo = todoMapper.todoDtoToTodo(todoDto);
    todo.setCompleted(false);
    if ("".equalsIgnoreCase(todo.getTitle())) {
      return null;
    }
    if (this.todoRepository.findByTitle(todo.getTitle()) != null) {
      return null;
    }
    Todo maxOrderTodo = this.todoRepository.findFirstByTitleIsNotNullOrderByOrderDesc();
    todo.setOrder((maxOrderTodo != null ? maxOrderTodo.getOrder() : 0) + 1);
    return this.todoRepository.save(todo);
  }

  public Todo updateTodo(TodoDto todoDto) {
    Todo todo = todoMapper.todoDtoToTodo(todoDto);
    if (this.todoRepository.findById(todo.getId()) == null) {
      return null;
    }
    if (this.todoRepository.findByTitleAndIdNot(todo.getTitle(), todo.getId()) != null) {
      return null;
    }
    return this.todoRepository.save(todo);
  }

  public boolean deleteTodo(int id) {
    Todo todo = this.todoRepository.findById(id);
    if (todo != null) {
      if (needToChangeOrderTodos(todo)) {
        int maxOrder = this.todoRepository.findFirstByTitleIsNotNullOrderByOrderDesc().getOrder();
        updateOrderTodos(todo.getOrder(), maxOrder, false);
      }
      this.todoRepository.deleteById(id);
      return true;
    }
    return false;
  }

  private boolean needToChangeOrderTodos(Todo todo) {
    int maxOrder = this.todoRepository.findFirstByTitleIsNotNullOrderByOrderDesc().getOrder();
    return maxOrder != todo.getOrder();
  }

  private void updateOrderTodos(int from, int to, boolean isIncrease) {
    this.todoRepository.findByOrderBetween(from, to)
        .forEach((curTodo) -> {
          if (isIncrease) {
            curTodo.setOrder(curTodo.getOrder() + 1);
          } else {
            curTodo.setOrder(curTodo.getOrder() - 1);
          }
          this.todoRepository.save(curTodo);
        });
  }

  public StatisticDto getStatistics() {
    int total = this.todoRepository.countAllByTitleIsNotNull();
    if (total != 0) {
      return StatisticDto.builder().remaining(this.remainingStatistic(total))
          .completed(this.completedStatistic(total)).build();
    }
    return null;
  }

  private Statistic remainingStatistic(int total) {
    int count = this.todoRepository.countAllByCompleted(false);
    int percent = Calculator.calculatePercentage(count, total);
    return new Statistic("remaining", count, percent, "background-color-red");
  }

  private Statistic completedStatistic(int total) {
    int count = this.todoRepository.countAllByCompleted(true);
    int percent = Calculator.calculatePercentage(count, total);
    return new Statistic("completed", count, percent, "background-color-green");
  }

  public boolean changeOrderTodo(ChangeOrderDto changeOrderDto) {
    int newOrder = changeOrderDto.getNewOrder();
    int id = changeOrderDto.getTodoId();
    if (!isValidOrder(newOrder)) {
      return false;
    }
    Todo todo = this.todoRepository.findById(id);
    if (todo != null && isDifferentOrder(todo, newOrder)) {
      if (todo.getOrder() > newOrder) {
        updateOrderTodos(newOrder, todo.getOrder(), true);
      } else {
        updateOrderTodos(todo.getOrder(), newOrder, false);
      }
      todo.setOrder(newOrder);
      this.todoRepository.save(todo);
      return true;
    }
    if (todo != null && !isDifferentOrder(todo, newOrder)) {
      return true;
    }
    return false;
  }

  private boolean isDifferentOrder(Todo todo, int newOrder) {
    return todo.getOrder() != newOrder;
  }

  private boolean isValidOrder(int order) {
    if (order <= 0) {
      return false;
    }
    return this.todoRepository.findFirstByTitleIsNotNullOrderByOrderDesc().getOrder() >= order;
  }
}
