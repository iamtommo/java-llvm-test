# deps
llvm
bash -c "$(wget -O - https://apt.llvm.org/llvm.sh)""

apt install build-essential
apt install libc6-dev

# compile IR to asm
llc <bitcode file> -o filename -filetype asm

# compile IR to obj
llc <bitcode file> -o filename -filetype obj

# linking
"ld.lld" file -e main

must link against libc
"ld.lld" -dynamic-linker /lib64/ld-linux-x86-64.so.2 /usr/lib/x86_64-linux-gnu/crt1.o /usr/lib/x86_64-linux-gnu/crti.o -L/usr/lib/x86_64-linux-gnu/ -lc kernel.o -e main /usr/lib/x86_64-linux-gnu/crtn.o

`nm` or `readelf -a or -h` file to inspect

# disassemble bitcode
llvm-dis file.bc

# debugging executable
readelf -a file

# helpful for seeing c->llvm ir
http://ellcc.org/demo/index.cgi