import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Cerificate, CertificateRequest, CertificateService } from '../services/certificate.service';

@Component({
  selector: 'app-request-review',
  templateUrl: './request-review.component.html',
  styleUrls: ['./request-review.component.css']
})
export class RequestReviewComponent {

  _role: String = 'ROLE_ADMIN';
  currentOption: string = "pending";
  areByMe: boolean = true;
  requests: CertificateRequest[] = []
  selectedRequestId: number = -1;
  pendingRequests: CertificateRequest[] = []
  notPendingRequests: CertificateRequest[] = []
  

  constructor(private authService: AuthService, private certificateService: CertificateService) {
      this._role = this.authService.getRole();
      console.log(this._role)
  }

  ngOnInit(): void {
    if (this._role == 'ROLE_USER') {
      this.certificateService.getIsByMeSelected().subscribe({
        next: (res) => {
          this.areByMe = res;
          if(this.areByMe) {
            this.getRequestsByMe()
          }
          else {
            this.getRequestsFromMe()
          }
        }
      });
    }
    else {
      this.certificateService.getAllRequestes().subscribe({
        next: (res) => {
          console.log(res);
          this.separateRequests(res);
        }
      });
    }
  }


  getRequestsByMe() {
    this.certificateService.getAllRequestesByMe().subscribe({
      next: (res) => {
        this.separateRequests(res);
      },
      error: (error: any) => {
        this.requests = [];
      }
    });
  }

  getRequestsFromMe() {
    this.certificateService.getAllRequestesFromMe().subscribe({
      next: (res) => {
        this.separateRequests(res);
      },
      error: (error: any) => {
        this.requests = [];
      }
    });
  }

  separateRequests(res :CertificateRequest[]) {
    this.pendingRequests = [];
    this.notPendingRequests = [];
    for (let req of res) {
      if (req.status == 'PENDING') {
        this.pendingRequests.push(req);
      }
      else {
        this.notPendingRequests.push(req);
      }
    }

    if (this.currentOption == 'pending') {
      this.requests = this.pendingRequests;
    }
    else {
      this.requests = this.notPendingRequests;
    }
  }


  changeTab(option : string) {
    this.currentOption = option;
    if (this.currentOption == 'pending') {
      this.requests = this.pendingRequests;
    }
    else {
      this.requests = this.notPendingRequests;
    }
  }

  public formatDate(dateStr: string): string {
    let date = new Date(dateStr);
    date.setMonth(date.getMonth() + 1);
    if (date.getMonth() == 0)
      date.setMonth(1);
    return "at " + date.getHours() + ":" + date.getMinutes() + ", " + date.getDate() + "." + date.getMonth() + "." + date.getFullYear();
  }

  selectRequest(request: CertificateRequest) {
    this.selectedRequestId = request.id;
  }


  acceptRequest() {
    if (this.selectedRequestId != -1) {
      this.certificateService.acceptRequestes(this.selectedRequestId).subscribe({
        next: (res) => {
          console.log(res);
        },
        error: (error:any) => {
          console.log(error)
        }
      })
    }
  }

  declineRequest() {
    if (this.selectedRequestId != -1) {
      this.certificateService.declineRequestes(this.selectedRequestId).subscribe({
        next: (res) => {
          console.log(res);
        },
        error: (error:any) => {
          console.log(error)
        }
      })
    }
  }
}
