# Tabula Java Runnable

This is a simple project that uses [Tabula Java](https://github.com/tabulapdf/tabula-java) to extract PDF documents into JSON.
Tabula JAVA can do much more, I have taken up these sensible defaults which can easily be converted into arguments. 
The reasoning behind this is Tabula does not provide any interface where
we can create our own say custom web API. 
The tabula team is working on this feature but it is not yet complete. 
When they are done obviously that will be a much more robust solution. Meanwhile this can be used, keep in mind the input
has to be Base 64 string representation of a PDF. For running this code I have added a sample data.txt file which will then be readback to PDF and converted to JSON.
I have also integrated Spring Boot to serve as an endpoint for this process. It listens for HTTP Post at localhost:8080/extract