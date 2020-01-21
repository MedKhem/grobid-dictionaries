## Service mode (GUI)

After making sure that a model exists for each segmentation level you could start using the GUI to segment your PDF dictionaries

After running jetty with the following command:
```bash
> mvn -DskipTests jetty:run-war
```

You can see the running application in your web browser: 

* For Windows, your 8080 port should be free to see the web application on the address:
```http://192.168.99.100:8080```

* For MacOs, the web application is running on the address:   
```http://localhost:8080```

To shutdown the server, you need to press 
```ctrl + c```


`Process Full Dictionary` corresponds to the cascading execution of all existing models to segment the input file. For the time being, the **Form** model is the only model adapted at this level. So please make sure to keep it the only model activated, as it is by default.  





 