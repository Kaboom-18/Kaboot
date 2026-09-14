🐧 Kaboot

Run Linux userspace environments directly on Android.

Kaboot is an Android application built with Java and C that provides a flexible Linux environment on modern Android devices without requiring root.

It combines PRoot, "ptrace()", native C process management, and Android services to create and manage Linux userspace sessions.

---

✨ Features

- 🐧 Install Linux distributions from the cloud
- 🔀 Run multiple Linux sessions simultaneously
- 🖥️ Run desktop environments and GUI applications
- 🍷 Experiment with Windows applications and games through Wine
- 🔐 Security-learning environments and tools
- 📦 Linux filesystem and cache management
- 🔔 Persistent sessions using an Android foreground service
- 🎨 Custom terminal fonts, colors, and appearance
- ⚙️ Configurable Linux environments
- 🔄 Session lifecycle management
- 📱 Designed for modern Android versions
- 🚀 Native C + Java integration

---

🧠 How It Works

Kaboot does not boot a separate Linux kernel.

Instead, it runs a Linux userspace inside Android using PRoot.

                 Android
                    │
                    ▼
              Kaboot (Java)
                    │
                    ▼
              Native C layer
                    │
                    ▼
                  PRoot
                    │
              ┌─────┴─────┐
              │  ptrace() │
              └─────┬─────┘
                    │
                    ▼
             Linux processes
                    │
                    ▼
          Linux root filesystem
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
      Shell       GUI          Apps

PRoot

PRoot provides userspace implementations of functionality commonly associated with:

- "chroot"
- Bind mounts
- Filesystem path translation
- Execution environment manipulation

It does this without requiring traditional root privileges.

"ptrace()"

PRoot relies heavily on Linux's "ptrace()" mechanism.

The tracer can observe and control processes and handle system-call behavior. This allows PRoot to translate paths and emulate parts of the environment expected by programs running inside the Linux root filesystem.

Kaboot manages these processes and their lifecycle from the Android application.

---

⚙️ Process Management

The native layer works with low-level Linux APIs such as:

fork();
execvp();
execve();
waitpid();
ptrace();
pipe();
dup2();
kill();

A simplified process flow:

fork()
  │
  ├── Parent
  │
  └── Child
       │
       ├── Configure pipes / PTY
       ├── Configure environment
       └── execvp()
              │
              ▼
          Linux program

"fork()" creates the process, "exec*()" replaces the process image, "pipe()" and "dup2()" provide communication and I/O redirection, while "waitpid()" and "kill()" help manage the process lifecycle.

---

📱 Modern Android

Kaboot is designed with modern Android restrictions in mind.

Older Linux-on-Android applications sometimes relied on targeting API 28 and older execution behavior.

Android 10 introduced W^X-related restrictions that prevent applications targeting API 29+ from directly executing binaries from writable application directories.

Kaboot does not depend on simply keeping the application at an old target SDK.

Instead, its architecture is designed around the execution and filesystem restrictions present on modern Android.

---

🖥️ Desktop Environments

Kaboot can be used to experiment with Linux desktop environments running inside the userspace environment.

Depending on the selected environment and configuration, users can run:

- Desktop environments
- Window managers
- GUI applications
- Development tools
- Terminal applications
- Wine-based applications

---

🍷 Wine

Kaboot can also be used to experiment with Wine inside supported Linux environments.

This makes it possible to explore running some Windows applications and games from an Android device.

Compatibility and performance depend on the device, architecture, Linux distribution, Wine configuration, and application requirements.

---

🔐 Security Learning

Kaboot can provide Linux security-testing environments for educational and authorized testing.

Tools such as Metasploit can be used inside the Linux environment for learning about:

- Network security
- Vulnerability research
- Penetration-testing concepts
- Security tooling
- Linux security

Only use security tools against systems you own or have explicit permission to test.

---

🏗️ Technology Stack

Component| Technology
Android application| Java
Native layer| C
Linux environment| PRoot
Process tracing| "ptrace()"
Process creation| "fork()"
Program execution| "execvp()" / "execve()"
IPC| Pipes / PTY
Background sessions| Android Foreground Service
Windows compatibility| Wine
Linux distributions| Cloud-installed root filesystems

---

🎯 Project Goals

Kaboot is more than a terminal emulator.

The project is a practical exploration of:

- Linux internals
- Operating-system concepts
- Process management
- System calls
- "ptrace()"
- IPC
- Filesystem virtualization
- Native C programming
- JNI / Java ↔ C integration
- Android security restrictions
- Linux userspace execution

---

🚧 Development Status

Kaboot is actively being developed.

Features, supported distributions, desktop environments, and compatibility may change as development continues.

---

📜 Credits

See "CREDITS" (CREDITS) for credits and acknowledgements for the libraries, projects, and resources used by Kaboot.

📄 License

See "LICENSE" (LICENSE) for the project's license and licensing terms.

---

👨‍💻 Author

Bishal Poudel

Built with Java, C, Linux, and Android.

«Linux userspace. Android device. No root required. 🐧📱»