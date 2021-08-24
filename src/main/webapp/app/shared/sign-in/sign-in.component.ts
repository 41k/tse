import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { LoginService } from '../../core/login/login.service';

@Component({
  selector: 'jhi-sign-in',
  templateUrl: './sign-in.component.html',
})
export class SignInComponent {
  login = '';
  password = '';
  authenticationError = false;

  constructor(private loginService: LoginService, private router: Router) {}

  signIn(): void {
    if (this.login.length === 0 || this.password.length === 0) {
      this.authenticationError = true;
    }
    this.loginService
      .login({
        username: this.login,
        password: this.password,
        rememberMe: false,
      })
      .subscribe(
        account => {
          this.authenticationError = false;
          if (account) {
            const authorities = account.authorities;
            if (authorities.includes('ROLE_USER')) {
              this.router.navigate(['strategy-executions']);
            }
            if (authorities.includes('ROLE_ADMIN')) {
              this.router.navigate(['admin/metrics']);
            }
          } else {
            this.router.navigate(['sign-in']);
          }
        },
        () => (this.authenticationError = true)
      );
  }
}
