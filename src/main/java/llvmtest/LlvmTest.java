package magnum;

import magnum.lex.Lexer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTargetRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.bytedeco.llvm.global.LLVM.*;

public class LlvmTest {

	public static void main(String[] args) throws Exception {
		var file = Paths.get(Mgc.class.getClassLoader().getResource("kernel.mg").toURI());
		var fileContents = Files.readString(file, StandardCharsets.UTF_8);
		var tokens = Lexer.lex(fileContents);

		//LLVMLinkInMCJIT();
		LLVMInitializeNativeAsmPrinter();
		LLVMInitializeNativeAsmParser();
		LLVMInitializeNativeDisassembler();
		LLVMInitializeNativeTarget();
		var ctx = LLVMContextCreate();
		LLVMModuleRef mod = LLVMModuleCreateWithName("mag_module");

		// exit
		var fn_exit_param_types = new LLVMTypeRef[] { LLVMInt32Type() };
		var fn_exit_type = LLVMFunctionType(LLVMVoidType(), new PointerPointer(fn_exit_param_types), 1, 0);
		var fn_exit = LLVMAddFunction(mod, "exit", fn_exit_type);
		LLVMSetFunctionCallConv(fn_exit, LLVMCCallConv);

		// printf
		var printf_paramtypes = new LLVMTypeRef[] { LLVMPointerType(LLVMInt8Type(), 0) };
		var printf_type = LLVMFunctionType(LLVMInt32Type(), new PointerPointer(printf_paramtypes/*LLVMPointerType(LLVMInt8Type(), 0)*/), 1, 1);
		var printf_fn = LLVMAddFunction(mod, "printf", printf_type);
		LLVMSetFunctionCallConv(printf_fn, LLVMCCallConv);


		// main
		var fntype = LLVMFunctionType(LLVMInt32Type(), new PointerPointer(), 0, 0);
		var mainfn = LLVMAddFunction(mod, "main", fntype);
		LLVMSetFunctionCallConv(mainfn, LLVMCCallConv);

		var entry = LLVMAppendBasicBlock(mainfn, "start");

		var builder = LLVMCreateBuilder();
		LLVMPositionBuilderAtEnd(builder, entry);


		//var chars = new LLVMValueRef[] { LLVMConstInt(LLVMInt8Type(), 54, 0), LLVMConstInt(LLVMInt8Type(), 54, 0), LLVMConstInt(LLVMInt8Type(), 0, 0)};
		//var chars_array = LLVMConstArray(LLVMInt8Type(), new PointerPointer(chars), 3);
		//LLVMAppendModuleInlineAsm(mod, "asm", );
		var bigstrink = LLVMBuildGlobalStringPtr(builder, new BytePointer("bigstrink"), new BytePointer("bigstrink2"));
		LLVMBuildCall(builder, printf_fn, new PointerPointer(new LLVMValueRef[] { bigstrink }), 1, "printf2");

		var exitcode = LLVMConstInt(LLVMInt32Type(), 0, 0);
		LLVMBuildCall(builder, fn_exit, new PointerPointer(new LLVMValueRef[] { exitcode }), 1, "");

		// obsolete, exit syscall never returns. necessary though.
		var ret = LLVMConstInt(LLVMInt32Type(), 420, 0);
		LLVMBuildRet(builder, ret);

		BytePointer error = new BytePointer((Pointer)null);
		LLVMVerifyModule(mod, LLVMAbortProcessAction, error);
		LLVMDisposeMessage(error);

		/*LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
		if(LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
			System.err.println(error.getString());
			LLVMDisposeMessage(error);
			System.exit(-1);
		}*/

		LLVMWriteBitcodeToFile(mod, "./target/kernel.bc");

		//var default_triple = LLVMGetDefaultTargetTriple();
		System.out.println("default triple: " + LLVMGetDefaultTargetTriple().getString());
		var target_triple = "x86_64-pc-linux-gnu";
		LLVMTargetRef target = new LLVMTargetRef();
		var err_targ_from_triple = new BytePointer((Pointer) null);
		if (LLVMGetTargetFromTriple(new BytePointer(target_triple), target, err_targ_from_triple) != 0) {
			System.err.println("err get target: " + err_targ_from_triple.getString());
			System.exit(-1);
		}
		LLVMDisposeMessage(err_targ_from_triple);

		var target_machine_ref = LLVMCreateTargetMachine(target, target_triple, "", "",
				LLVMCodeGenLevelDefault, LLVMRelocDefault, LLVMCodeModelJITDefault);

		///////--------- WRITING BINARY
		/*System.out.println("writing binary");
		var bitcodebuf = LLVMWriteBitcodeToMemoryBuffer(mod);
		var err_mkbinary = new BytePointer((Pointer) null);
		var binary = LLVMCreateBinary(bitcodebuf, ctx, err_mkbinary);
		if (binary == null || binary.isNull()) {
			System.err.println("err making binary: " + err_mkbinary.getString());
			System.exit(-1);
		}
		var binary_buffer = binary.asByteBuffer();
		var file_channel = new FileOutputStream(new File("./target/kernel")).getChannel();
		file_channel.write(binary_buffer);
		file_channel.close();
		System.out.println("success wrote binary type " + LlvmBinaryTypeToString(LLVMBinaryGetType(binary)) + " total size size " + binary_buffer.position());

		//elf64-x86-64
		LLVMDisposeMemoryBuffer(bitcodebuf);
		LLVMDisposeMessage(err_mkbinary);*/


		/////----- DIRECTLY EMIT MACHINE CODE
		/*System.out.println("emitting machine code");
		var err_emit = new BytePointer((Pointer) null);
		if (LLVMTargetMachineEmitToFile(target_machine_ref, mod, new BytePointer("./target/kernelmc.s"), LLVMCodeGenLevelDefault, err_emit) != 0) {
			System.err.println("err emit: " + err_emit.getString());
			System.exit(-1);
		}
		LLVMDisposeMessage(err_emit);*/

		//////----- ORC JIT
		/*LLVMOrcJITStackRef orcref = LLVMOrcCreateInstance(target_machine_ref);
		LLVMOrcAddLazilyCompiledIR(orcref, new LongPointer(mod), new LLVMOrcSymbolResolverFn() {

		}, (Pointer) null);

		/*private static class OrcFnResolver extends LLVMOrcSymbolResolverFn {
		LLVMOrcJITStackRef orc;
		@Override
		public @Cast("uint64_t") long call(@Cast("const char*") BytePointer Name, Pointer LookupCtx) {
			LLVMOrcGetSymbolAddress(orc, Name);
		}
	}*/

		LLVMDisposeBuilder(builder);
		LLVMContextDispose(ctx);
	}

	/*
	var paramTypes = new LLVMTypeRef[] { LLVMInt32Type(), LLVMInt32Type() };
		var returnType = LLVMFunctionType(LLVMInt32Type(), new PointerPointer(paramTypes), 2, 0);
		var sum = LLVMAddFunction(mod, "sum", returnType);
		LLVMSetFunctionCallConv(sum, LLVMCCallConv);

		var entry = LLVMAppendBasicBlock(sum, "entry");

		var builder = LLVMCreateBuilder();
		LLVMPositionBuilderAtEnd(builder, entry);

		var tmp = LLVMBuildAdd(builder, LLVMGetParam(sum, 0), LLVMGetParam(sum, 1), "tmp");
		LLVMBuildRet(builder, tmp);

		BytePointer error = new BytePointer((Pointer)null);
		LLVMVerifyModule(mod, LLVMAbortProcessAction, error);
		LLVMDisposeMessage(error);

		LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
		if(LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
			System.err.println(error.getString());
			LLVMDisposeMessage(error);
			System.exit(-1);
		}

		LLVMWriteBitcodeToFile(mod, "./target/kernel.bc");

		var sumArgs = new LLVMGenericValueRef[] { LLVMCreateGenericValueOfInt(LLVMInt32Type(), 69, 0),
				LLVMCreateGenericValueOfInt(LLVMInt32Type(), 1337, 0)};

		var result = LLVMRunFunction(engine, sum, 2, new PointerPointer(sumArgs));
		System.out.println("result " + LLVMGenericValueToInt(result, 0));

		LLVMDisposeBuilder(builder);
		LLVMDisposeExecutionEngine(engine);
	 */

	public static String LlvmBinaryTypeToString(int t) {
		switch (t) {
			case 3:
				return "LLVMIR";
			case 8:
				return "ELF64L";
			case 9:
				return "ELF64B";
			default:
				return String.valueOf(t);
		}
	}
}
