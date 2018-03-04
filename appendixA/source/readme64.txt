This project was compiled in x64 on Windows7, using Visual Studio 2012, Eclipse Mars and Python 2.7.

C++ wx 2.8.12:
	-in VS command prompt:	call '"%VS110COMNTOOLS%"vsvars32.bat'
							call '"%VCINSTALLDIR%"vcvarsall.bat x64'
	-in VS command prompt (from wxWidgets-2.8.12\build\msw): 'nmake -f makefile.vc BUILD=release TARGET_CPU=AMD64 SHARED=0 MONOLITHIC=0 UNICODE=0 USE_OPENGL=1 CXXFLAGS="/DNEED_PBT_H"'
	
Bezier:
	-SET VS90COMNTOOLS=%VS110COMNTOOLS%
	-Copy contents of 'C:\Python27\libs\python27' to 'C:\Python27\libs\python27_d'
	-Generate C code: 'python setup.py build_ext --inplace' ("error: command 'mt.exe' failed" can be ignored)
	-copy cyBezier.pyd dll: 'copy /Y .\bezier\cyBezier.pyd .\VTL\x64\debug\ & copy /Y .\bezier\cyBezier.pyd .\VTL\x64\release\ & copy /Y .\bezier\cyBezier.pyd .\NativeLib\ & copy /Y .\bezier\cyBezier.pyd .\agent\'

C++ VTL:
	-build as static library (VTL.lib) in VS
	
NativeLib:
	-build .class file: 'javac src\nativelib\VTL.java -d bin\' from 'NativeLib\'
	-set PATH=C:\Program Files\Java\jdk1.7.0_79\bin
	-build c++ header: 'javah -jni nativelib.VTL' from '\NativeLib\bin\'

C++ NativeInterface
	-build as dynamic library

Agent:
	-compile in Eclipse
				
Distribute Agent.java with .dll and .pyd files