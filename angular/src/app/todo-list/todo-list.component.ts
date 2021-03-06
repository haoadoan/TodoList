import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { TodoModel } from 'src/models/TodoModel';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {

  @Input() todos: TodoModel[];
  @Input() enableDrag: boolean;

  @Output() removeTodo = new EventEmitter();
  @Output() updateTodo = new EventEmitter();
  @Output() itemReorder = new EventEmitter();

  constructor() {
  }

  ngOnInit() {
  }

  remove(todo: TodoModel) {
    this.removeTodo.next(todo);
  }

  update(todo: TodoModel) {
    this.updateTodo.next(todo);
  }

  reorder(data) {
    this.itemReorder.next(data);
  }
}
