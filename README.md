#Tabula Java Runnable

This is a simple project that uses [Tabula Java](https://github.com/tabulapdf/tabula-java) to extract PDF documents into JSON.
Tabula JAVA can do much more, I have taken up these sensible defaults which can easily be converted into arguments. The reasoning behind this is Tabula does not provide any interface where
we can create our own say custom web API. The tabula team is working on this feature but it is not yet complete. When they are done obviously that will be a much more robust solution. Meanwhile this can be used.
I have also converted the input to be Base 64 string which can be passed down in any form and converted to a string. For running this code I have added a sample data.txt file which will then be readback to PDF and converted to JSON in the form of an output file. You can modify this behavior and send the output as a result of a web endpoint. For example you can take this code and embed it in a Spring Boot Application. 