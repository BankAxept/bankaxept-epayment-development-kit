package no.bankaxept.epayment.client.base;

public enum RequestStatus {
  Accepted, // Event is accepted successfully (and stored)
  Repeated, // Event is a repeat of a previously accepted event.
  Rejected, // Event is rejected (not allowed for the current state)
  Conflicted, // Event conflicts with a previous event (message id was reused)
  Failed // Event processing failed (temporarily, can try again)
}
