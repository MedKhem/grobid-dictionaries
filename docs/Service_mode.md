## Service mode (GUI)

After making sure that a model exists for each segmentation level you could start using the GUI to segment your PDF dictionaries

After running jetty with the following command:
```bash
> mvn -Dmaven.test.skip=true jetty:run-war
```

the web service would be accessible directly at the navigator (if you run it locally):
```bash
> http://localhost:8080/
```

For the time being, `Process Full Dictionary` corresponds to the cascading execution of all existing models to segment the input file. The rest of the labels corresponds to the execution of the first  segmentation models.   
