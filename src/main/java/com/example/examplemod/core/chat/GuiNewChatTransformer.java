// src/main/java/com/example/examplemod/core/chat/GuiNewChatTransformer.java
package com.example.examplemod.core.chat;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class GuiNewChatTransformer implements IClassTransformer {

    private static final String TARGET = "net.minecraft.client.gui.GuiNewChat";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {

        if (bytes == null) return null;
        if (!TARGET.equals(transformedName)) return bytes;

        System.out.println("[HexChat] Transforming GuiNewChat…");

        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        ClassVisitor cv = new ClassVisitor(ASM4, cw) {

            @Override
            public MethodVisitor visitMethod(int access, String mName, String desc,
                                             String sig, String[] exceptions) {

                MethodVisitor mv = super.visitMethod(access, mName, desc, sig, exceptions);

                // func_146237_a(IChatComponent;IIZ)V
                if (!"func_146237_a".equals(mName)) return mv;
                if (!"(Lnet/minecraft/util/IChatComponent;IIZ)V".equals(desc)) return mv;

                System.out.println("[HexChat]   → Found func_146237_a, patching…");

                return new MethodVisitor(ASM4, mv) {

                    boolean injected = false;

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name,
                                                String methodDesc, boolean itf) {

                        // We only care that it's the static GuiUtilRenderComponents call.
                        boolean match =
                                opcode == INVOKESTATIC &&
                                        owner.equals("net/minecraft/client/gui/GuiUtilRenderComponents") &&
                                        name.equals("func_178908_a");

                        if (!injected && match) {
                            System.out.println("[HexChat]     Hooking result of GuiUtilRenderComponents.func_178908_a, desc=" + methodDesc);

                            // 1) original static call: stack now has the List from GuiUtilRenderComponents
                            super.visitMethodInsn(opcode, owner, name, methodDesc, itf);

                            // 2) wrap it in HexChatWrapFix.carryComponents(List) → List
                            super.visitMethodInsn(INVOKESTATIC,
                                    "com/example/examplemod/client/HexChatWrapFix",
                                    "carryComponents",
                                    "(Ljava/util/List;)Ljava/util/List;",
                                    false);

                            injected = true;
                            return; // don't fall through to super again
                        }

                        super.visitMethodInsn(opcode, owner, name, methodDesc, itf);
                    }

                    @Override
                    public void visitEnd() {
                        if (!injected) {
                            System.out.println("[HexChat]   ! WARNING: carryComponents hook NOT applied!");
                        }
                        super.visitEnd();
                    }
                };
            }

        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}
