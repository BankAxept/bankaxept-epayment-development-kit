
## Versioning

The dependency version of this repository follows the `MAJOR.MINOR.PATCH` format.

* **Major version**: This version is used in the URI, like `/v1/`, and indicates breaking changes to the API. Internally, the URI is utilized to route to the correct internal execution.
* **Minor and Patch versions**: These versions remain transparent in the API and are used internally for backward-compatible additions and updates.

Diverging from the above versioning scheme requires approval from all integrators.

## API Lifecycle

We strive to keep our API backwards compatible in order to minimize the impact on our integrators.
The following changes are considered backwards compatible.

### Expanding a request with an optional field.

As long as a field is optional it is considered backwards compatible to add it to a request. This means that the integrator can choose to ignore the field if it is not needed.
This should be unproblematic, but bear this constraint in mind regarding any automation and validation tied to our Swagger files.

### Expanding a response with an optional field.

As long as a field is optional it is considered backwards compatible to add it to a response. This means that the integrator can choose to ignore the field if it is not needed.
This should be unproblematic, but bear this constraint in mind regarding any automation and validation tied to our Swagger files. As well as overly strict response deserialization.

`Response parsing must be robust and ignore any unknown fields.`

### Relaxing validation rules
For example making the required length of a string shorter. This is considered backwards compatible as long as the integrator can still send the same data as before.

### Correcting bugs/errors that does not change intended behavior.

Correcting a bug, for example an incorrect/malformed error returned in an edge case, is considered backwards compatible as long as the integrator can still send the same data as before.
