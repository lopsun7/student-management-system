import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Employee, EmployeeApiService } from './employee-api.service';

type LoginMode = 'password' | 'gmail';

const emptyEmployee: Employee = {
  firstName: '',
  lastName: '',
  email: '',
  department: '',
};

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly employeeApi = inject(EmployeeApiService);

  readonly loginMode = signal<LoginMode>('password');
  readonly loading = signal(false);
  readonly message = signal('');
  readonly employees = signal<Employee[]>([]);
  readonly isLoggedIn = this.employeeApi.token;

  username = 'steven';
  password = 'password123';
  gmailEmail = 'steven.demo@gmail.com';
  formEmployee: Employee = { ...emptyEmployee };

  async ngOnInit(): Promise<void> {
    if (this.isLoggedIn()) {
      await this.loadEmployees();
    }
  }

  async loginWithPassword(): Promise<void> {
    await this.runAction(async () => {
      await firstValueFrom(this.employeeApi.loginWithPassword(this.username, this.password));
      await this.loadEmployees();
      this.message.set('Logged in with username/password.');
    }, 'Username/password login failed. Check the credentials and backend server.');
  }

  async loginWithGmail(): Promise<void> {
    await this.runAction(async () => {
      await firstValueFrom(this.employeeApi.loginWithGmail(this.gmailEmail));
      await this.loadEmployees();
      this.message.set('Logged in with Gmail demo account.');
    }, 'Gmail demo login failed. Use an address ending in @gmail.com.');
  }

  logout(): void {
    this.employeeApi.logout();
    this.employees.set([]);
    this.resetForm();
    this.message.set('Logged out.');
  }

  async loadEmployees(): Promise<void> {
    await this.runAction(async () => {
      this.employees.set(await firstValueFrom(this.employeeApi.getEmployees()));
    }, 'Could not load employees. Make sure the Spring Boot backend is running on port 8080.');
  }

  async saveEmployee(): Promise<void> {
    await this.runAction(async () => {
      const saved = this.formEmployee.id
        ? await firstValueFrom(this.employeeApi.updateEmployee(this.formEmployee))
        : await firstValueFrom(this.employeeApi.createEmployee(this.formEmployee));

      this.employees.update((current) => {
        const existingIndex = current.findIndex((employee) => employee.id === saved.id);
        if (existingIndex === -1) {
          return [...current, saved];
        }
        return current.map((employee) => (employee.id === saved.id ? saved : employee));
      });
      this.resetForm();
      this.message.set('Employee saved successfully.');
    }, 'Could not save employee. Check that all fields are valid.');
  }

  editEmployee(employee: Employee): void {
    this.formEmployee = { ...employee };
    this.message.set(`Editing #${employee.id}.`);
  }

  async deleteEmployee(employee: Employee): Promise<void> {
    if (!employee.id) {
      return;
    }
    await this.runAction(async () => {
      await firstValueFrom(this.employeeApi.deleteEmployee(employee.id!));
      this.employees.update((current) => current.filter((item) => item.id !== employee.id));
      if (this.formEmployee.id === employee.id) {
        this.resetForm();
      }
      this.message.set(`Deleted ${employee.firstName} ${employee.lastName}.`);
    }, 'Could not delete employee.');
  }

  resetForm(): void {
    this.formEmployee = { ...emptyEmployee };
  }

  private async runAction(action: () => Promise<void>, errorMessage: string): Promise<void> {
    this.loading.set(true);
    this.message.set('');
    try {
      await action();
    } catch {
      this.message.set(errorMessage);
    } finally {
      this.loading.set(false);
    }
  }
}
