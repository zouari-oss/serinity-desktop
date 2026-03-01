# HOW TO USE - Integrated Access Control Module + Mood Control Module

This guide explains how to install the required dependencies and run the integrated **Access Control** and **Mood Control** modules.

## Install SkinnedRatOrm

### Step 1: Download `settings.xml`

Download the required [settings.xml](https://raw.githubusercontent.com/zouari-oss/cpkg/refs/heads/main/templates/xml/settings.xml) file (provided by the project or repository).

### Step 2: Place `settings.xml` in the Maven `.m2` directory

Move the file to your Maven configuration directory:

#### Windows

```
C:\Users\<your-username>\.m2\settings.xml
```

Example:

```
C:\Users\zouari\.m2\settings.xml
```

#### 🐧 Linux

```
/home/<your-username>/.m2/settings.xml
```

Example:

```
/home/zouari/.m2/settings.xml
```

> [!NOTE]
> If the `.m2` folder does not exist, create it manually.

## Install OpenCV Library and Java JAR

The project requires OpenCV with Java support.

### Windows Installation

#### Step 1: Open PowerShell and go to Desktop

```powershell
cd $HOME/Desktop
```

#### Step 2: Download the installation script

```powershell
irm https://raw.githubusercontent.com/zouari-oss/cpkg/refs/heads/main/scripts/java/opencv4j2.ps1 -OutFile opencv4j2.ps1
```

#### Step 3: Run the script with `-All`

```powershell
powershell -ExecutionPolicy Bypass -File .\opencv4j2.ps1 -All
```

This will:

- Clone OpenCV
- Clone OpenCV Contrib
- Build OpenCV with Java support
- Install OpenCV

### Linux Installation

#### Step 1: Open Terminal and go to Download

```bash
cd ~/Downloads
```

#### Step 2: Download the installation script

```bash
curl -O https://raw.githubusercontent.com/zouari-oss/cpkg/refs/heads/main/scripts/java/opencv4j2.sh
```

Or:

```bash
wget https://raw.githubusercontent.com/zouari-oss/cpkg/refs/heads/main/scripts/java/opencv4j2.sh
```

#### Step 3: Make the script executable

```bash
chmod u+x opencv4j2.sh
```

#### Step 4: Run the script

```bash
./opencv4j2.sh --all
```

## After Installation (Windows & Linux)

Once the build finishes, locate the generated `.jar` file inside:

```
opencv/build/bin
```

Copy the `.jar` file into:

```
/project/access-control/lib
```

> [!NOTE]
> Your OpenCV Java environment is now ready for the **Access Control + Mood Control** integration.

## Installation Complete

You have now:

- Installed [SkinnedRatOrm](https://github.com/zouari-oss/skinned-rat-orm)
- Installed [OpenCV](https://github.com/opencv/opencv)
- Added the required OpenCV `.jar` to the project

> The integrated **Access Control + Mood Control** module is now ready to build and run.
> Happy Integration :)
