import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface AuthTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  issuedAt: string;
  expiresAt: string;
}

export interface Employee {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
}

@Injectable({ providedIn: 'root' })
export class EmployeeApiService {
  private readonly apiBaseUrl = 'http://localhost:8080/api/v1';
  readonly token = signal<string | null>(localStorage.getItem('employee-ui-token'));

  constructor(private readonly http: HttpClient) {}

  loginWithPassword(username: string, password: string): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>(`${this.apiBaseUrl}/auth/token`, { username, password })
      .pipe(tap((response) => this.saveToken(response.accessToken)));
  }

  loginWithGmail(email: string): Observable<AuthTokenResponse> {
    return this.http
      .post<AuthTokenResponse>(`${this.apiBaseUrl}/auth/gmail-demo`, { email })
      .pipe(tap((response) => this.saveToken(response.accessToken)));
  }

  logout(): void {
    localStorage.removeItem('employee-ui-token');
    this.token.set(null);
  }

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.apiBaseUrl}/employees`, {
      headers: this.authHeaders(),
    });
  }

  createEmployee(employee: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${this.apiBaseUrl}/employees`, employee, {
      headers: this.authHeaders(),
    });
  }

  updateEmployee(employee: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.apiBaseUrl}/employees/${employee.id}`, employee, {
      headers: this.authHeaders(),
    });
  }

  deleteEmployee(id: number): Observable<{ deleted: boolean }> {
    return this.http.delete<{ deleted: boolean }>(`${this.apiBaseUrl}/employees/${id}`, {
      headers: this.authHeaders(),
    });
  }

  private saveToken(accessToken: string): void {
    localStorage.setItem('employee-ui-token', accessToken);
    this.token.set(accessToken);
  }

  private authHeaders(): HttpHeaders {
    const accessToken = this.token();
    return accessToken
      ? new HttpHeaders({ Authorization: `Bearer ${accessToken}` })
      : new HttpHeaders();
  }
}
