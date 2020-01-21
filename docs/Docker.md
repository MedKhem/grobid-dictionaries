
1. Get docker 
 * For Windows 7 and 8 Users download from [this link](https://docs.docker.com/toolbox/toolbox_install_windows/)
 * For Windows 10 and later Users download (from Stable Channel) from [this link](https://docs.docker.com/docker-for-windows/install/)
 * For macOS Users download (from Stable Channel) from [this link](https://docs.docker.com/docker-for-mac/install/)
 * For Linux (Ubuntu) Users follow the instructions in [this link](https://docs.docker.com/engine/installation/linux/docker-ce/ubuntu/)
 
2. Run in your terminal (For Windows 7 and 8, run in Quickstart terminal / For Windows 10, run in Command Prompt) 
```bash
docker pull medkhem/grobid-dictionaries:onomacz
```
3. You need the 'toyData' directory to create dummy models. You could get it from the [github repository](https://github.com/MedKhem/grobid-dictionaries)
 

4. We could now run our image and having the 'toyData' and 'resources' as shared volumes between your machine and the container. Whatever you do in on of these directories, it's applied to both of them

* For macOS users:
```bash
docker run -v PATH_TO_YOUR_TOYDATA/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries:onomacz bash
```

* For Windows users: 
```bash
docker run -v //c/Users/YOUR_USERNAME/Desktop/toyData:/grobid/grobid-dictionaries/resources -p 8080:8080 -it medkhem/grobid-dictionaries:onomacz bash
```
