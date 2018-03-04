from distutils.core import setup
from Cython.Build import cythonize

setup(
  ext_modules = cythonize("cyBezier.pyx"),
)
#from distutils.core import setup
#from distutils.extension import Extension
#
#USE_CYTHON = True
#
#ext = '.pyx' if USE_CYTHON else '.c'
#
#extensions = [Extension("cyTest", ["cyTest"+ext])]
#
#if USE_CYTHON:
#    from Cython.Build import cythonize
#    extensions = cythonize(extensions)
#
#setup(
#    ext_modules = extensions
#)