/**
 * Copyright (c) 2014, Rudolf Schlatte. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package org.rpl.frontend.antlr.parser;

import org.rpl.frontend.ast.*;
import org.rpl.frontend.parser.ASTPreProcessor;
import org.rpl.frontend.parser.Main;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * This class creates the JastAdd AST from an Antlr parse tree.
 *
 * @author Rudi Schlatte
 */
public class CreateJastAddASTListener extends org.rpl.frontend.antlr.parser.RPLBaseListener {

    String filename = Main.UNKNOWN_FILENAME;

    /** maps antlr nodes to JastAdd nodes - see antlr book Sec.7.5 */
    ParseTreeProperty<ASTNode<?>> values = new ParseTreeProperty<>();
    UnresolvedTypeUse t = null;
    CompilationUnit result = null;

    public CreateJastAddASTListener(java.io.File filename) {
        if (filename != null) this.filename = filename.getPath();
    }

    private <T extends ASTNode<?>> T setASTNodePosition(ParserRuleContext node, T value) {
        assert node != null;
        Token start = node.getStart();
        Token stop = node.getStop();
        // for a completely empty file, CompilationUnit.stop will be null
        if (stop == null) stop = start;
        int startline = start.getLine();
        int startcol = start.getCharPositionInLine();
        int endline = stop.getLine();
        int endcol = stop.getCharPositionInLine() + stop.getText().length();
        value.setPosition(startline, startcol, endline, endcol);
        value.setFileName(this.filename);
        return value;
    }

    private <T extends ASTNode<?>> T setASTNodePosition(Token node, T value) {
        assert node != null;
        int startline = node.getLine();
        int startcol = node.getCharPositionInLine();
        // for a completely empty file, CompilationUnit.stop will be null
        int endline = startline;
        int endcol = startcol + node.getText().length();
        value.setPosition(startline, startcol, endline, endcol);
        value.setFileName(this.filename);
        return value;
    }

    /**
     * Associates JastAdd value with antlr node such that v(node) will
     * return value.  Also sets filename and position of the JastAdd
     * value.  Returns the passed-in JastAdd value.
     */
    private <T extends ASTNode<?>> T setV(ParserRuleContext node, T value) {
        setASTNodePosition(node, value);
        values.put(node, value);
        return value;
    }

    /**
     * Returns the AstNode for the given antlr parse node.  The result
     * is guaranteed to be non-null.
     */
    @SuppressWarnings("unchecked")
    private <T extends ASTNode<?>> T v(ParseTree node) {
        ASTNode<?> result = values.get(node);
        if (result == null) throw new NullPointerException();
        return (T) result;
    }

    /**
     * Returns a fresh Opt<ASTNode> filled with the result of v(node) if
     * node is non-null, empty otherwise.
     */
    private <T extends ASTNode<?>> Opt<T> o(ParseTree node) {
        if (node == null) return new Opt<>();
        else return new Opt<>(v(node));
    }

    /**
     * Returns a list of ASTNodes given a list of antlr parse nodes.
     * The result list elements are found via 'v' and are guaranteed
     * non-null.
     */
    private <T extends ASTNode<?>> List<T> l(java.util.List<? extends ParseTree> l) {
        List<T> result = new List<>();
        for (ParseTree n : l) {
            result.add(v(n));
        }
        return result;
    }

    public CompilationUnit getCompilationUnit() {
        return result;
    }

    private StringLiteral makeStringLiteral(String tokenText) {
        return new StringLiteral(ASTPreProcessor.preprocessStringLiteral(tokenText));
    }

    private PureExp makeTemplateStringLiteral(String tokenText) {
        return new StringLiteral(ASTPreProcessor.preprocessTemplateStringLiteral(tokenText));
    }

    @Override public void enterCompilation_unit(RPLParser.Compilation_unitContext ctx) {
        CompilationUnit r = new CompilationUnit();
        r.setName(this.filename);
        this.result = setV(ctx, r);
    }

    @Override public void exitCompilation_unit(RPLParser.Compilation_unitContext ctx) {
        result.setModuleDeclList(l(ctx.module_decl()));
        result.setDeltaDeclList(l(ctx.delta_decl()));
        result.setProductLineOpt(o(ctx.productline_decl()));
        result.setProductDeclList(l(ctx.product_decl()));
        result.setFeatureDeclList(l(ctx.feature_decl()));
        result.setFExtList(l(ctx.fextension()));
    }


    // Traits


    @Override public void exitDeltaTraitFragment(RPLParser.DeltaTraitFragmentContext ctx) {
        setV(ctx,new DeltaTraitModifier(v(ctx.trait_oper())));
    }

    @Override public void exitTraitAddFragment(RPLParser.TraitAddFragmentContext ctx) {
        setV(ctx,new AddMethodModifier((TraitExpr) v(ctx.basic_trait_expr())));
    }
    @Override public void exitTraitModifyFragment(RPLParser.TraitModifyFragmentContext ctx) {
        setV(ctx,new ModifyMethodModifier((TraitExpr) v(ctx.basic_trait_expr())));
     }
    @Override public void exitTraitRemoveFragment(RPLParser.TraitRemoveFragmentContext ctx) {
        List<MethodSig> l = new List<>();
        for (RPLParser.MethodsigContext methodSig : ctx.methodsig()) {
            l.add(v(methodSig));
        }
        setV(ctx,new RemoveMethodModifier(l));
     }

    @Override
    public void exitTrait_expr(RPLParser.Trait_exprContext ctx) {
        TraitExpr result = v(ctx.basic_trait_expr());
        for (RPLParser.Trait_operContext t : ctx.trait_oper()) {
            result = new TraitModifyExpr(result, v(t));
        }
        setV(ctx, result);
    }

    @Override
    public void exitTraitNameFragment(RPLParser.TraitNameFragmentContext ctx) {
        setV(ctx, new TraitNameExpr(ctx.TYPE_IDENTIFIER().getText()));
    }
    @Override
    public void exitTraitSetFragment(RPLParser.TraitSetFragmentContext ctx) {
        setV(ctx, new TraitSetExpr(l(ctx.method())));
    }
    @Override public void exitTrait_usage( RPLParser.Trait_usageContext ctx) {
        //setV(ctx, new TraitUse(ctx.TYPE_IDENTIFIER().toString(), new List()));
        setV(ctx, new TraitUse(v(ctx.trait_expr())));
    }
    @Override public void exitTrait_decl( RPLParser.Trait_declContext ctx) {
        setV(ctx, new TraitDecl(ctx.qualified_type_identifier().getText(), v(ctx.annotations()), v(ctx.trait_expr())));
    }

    // Declarations
    @Override public void exitDecl(RPLParser.DeclContext ctx) {
        setV(ctx, v(ctx.getChild(0))); // relies on decl having one token
    }

    @Override public void exitModule_decl(RPLParser.Module_declContext ctx) {
        setV(ctx, new ModuleDecl(ctx.qualified_type_identifier().getText(), l(ctx.exports), l(ctx.imports), l(ctx.decl()), o(ctx.main_block())));
    }

    @Override public void exitModule_export(RPLParser.Module_exportContext ctx) {
        if (ctx.f == null) {
            if (ctx.s == null || ctx.s.isEmpty()) setV(ctx, new StarExport());
            else setV(ctx, new NamedExport(l(ctx.s)));
        } else {
            if (ctx.s == null || ctx.s.isEmpty()) setV(ctx, new StarExport(o(ctx.f)));
            else setV(ctx, new FromExport(l(ctx.s), ctx.f.getText()));
        }
    }

    @Override public void exitModule_import(RPLParser.Module_importContext ctx) {
        if (ctx.s == null || ctx.s.isEmpty()) setV(ctx, new StarImport(ctx.f.getText()));
        else  if (ctx.f == null) setV(ctx, new NamedImport(l(ctx.s)));
        else setV(ctx, new FromImport(l(ctx.s), ctx.f.getText()));
    }

    @Override public void exitDatatype_decl(RPLParser.Datatype_declContext ctx) {
        ParametricDataTypeDecl d = setV(ctx, new ParametricDataTypeDecl(ctx.n.getText(), l(ctx.c), v(ctx.annotations()),
            new List<>()));
        for (Token t : ctx.p) {
            TypeParameterDecl tpd = new TypeParameterDecl(t.getText());
            setASTNodePosition(t, tpd);
            d.addTypeParameter(tpd);
        }
    }

    @Override public void exitData_constructor(RPLParser.Data_constructorContext ctx) {
        DataConstructor d
            = setV(ctx, new DataConstructor(ctx.n.getText(), new List<>()));
        // KLUDGE: copied into exitException_decl
        for (RPLParser.Data_constructor_argContext a : ctx.a) {
            final TypeUse vt = v(a.type_use());
            final DataTypeUse vtresolved;
            if (vt instanceof DataTypeUse) {
                vtresolved = (DataTypeUse) vt;
            } else {
                // See below, we may be facing an UnresolvedTypeUse.
                assert vt instanceof UnresolvedTypeUse : vt.getClass().getName();
                vtresolved = new DataTypeUse(vt.getName(), vt.getAnnotations());
                vtresolved.setPositionFromNode(vt);
            }
            ConstructorArg ca = new ConstructorArg(vtresolved, a.IDENTIFIER() != null ? new Opt<>(new Name(a.IDENTIFIER().getText())) : new Opt<>());
            setASTNodePosition(a, ca);
            d.addConstructorArg(ca);
        }
    }

    @Override public void exitFunction_decl(RPLParser.Function_declContext ctx) {
        // TODO: datatypes do not distinguish between DataTypeDecl and
        // ParametricDataTypeDecl; it would be nice to do this for
        // functions as well.
        FunctionDef d;
        if (ctx.e == null) {
            BuiltinFunctionDef bd = new BuiltinFunctionDef();
            if (ctx.pure_exp_list() != null) {
                bd.setArgumentList(v(ctx.pure_exp_list()));
            }
            d = bd;
        } else {
            d = new ExpFunctionDef(v(ctx.e));
        }
        List<ParamDecl> p = v(ctx.paramlist());
        TypeUse t = v(ctx.type_use());
        if (ctx.p != null && !ctx.p.isEmpty()) {
            ParametricFunctionDecl dp
                = setV(ctx, new ParametricFunctionDecl(ctx.n.getText(), t, p, d, v(ctx.annotations()),
                new List<>()));
            for (Token tp : ctx.p) {
                TypeParameterDecl tpd = new TypeParameterDecl(tp.getText());
                setASTNodePosition(tp, tpd);
                dp.addTypeParameter(tpd);
            }
        } else {
            setV(ctx, new FunctionDecl(ctx.n.getText(), v(ctx.annotations()), t, p, d));
        }
    }

    @Override
    public void exitPar_function_decl(RPLParser.Par_function_declContext ctx) {
        PartialFunctionDef d = new PartialFunctionDef(v(ctx.e));
        List<ParamDecl> params = v(ctx.params);
        List<FunctionParamDecl> funcParams = v(ctx.functions);
        TypeUse t = v(ctx.type_use());
        if(ctx.p != null && !ctx.p.isEmpty()) {
            ParametricPartialFunctionDecl fd =
                setV(ctx, new ParametricPartialFunctionDecl(ctx.n.getText(), l(ctx.annotation()),
                    new List<>(), t, params, funcParams, d));
            for (Token tp : ctx.p) {
                TypeParameterDecl tpd = new TypeParameterDecl(tp.getText());
                setASTNodePosition(tp, tpd);
                fd.addTypeParameter(tpd);
            }
        } else {
            setV(ctx, new PartialFunctionDecl(ctx.n.getText(), l(ctx.annotation()), t, params, funcParams, d));
        }
    }

    @Override public void exitTypesyn_decl(RPLParser.Typesyn_declContext ctx) {

        setV(ctx, new TypeSynDecl(ctx.qualified_type_identifier().getText(), v(ctx.annotations()), v(ctx.type_use())));
    }

    @Override public void exitException_decl(RPLParser.Exception_declContext ctx) {
        ExceptionConstructor d
            = new ExceptionConstructor(ctx.n.getText(), new List<>());
        // KLUDGE: copy of exitData_constructor
        for (RPLParser.Data_constructor_argContext a : ctx.a) {
            final TypeUse vt = v(a.type_use());
            final DataTypeUse vtresolved;
            if (vt instanceof DataTypeUse) {
                vtresolved = (DataTypeUse) vt;
            } else {
                // See below, we may be facing an UnresolvedTypeUse.
                assert vt instanceof UnresolvedTypeUse : vt.getClass().getName();
                vtresolved = new DataTypeUse(vt.getName(), vt.getAnnotations());
                vtresolved.setPositionFromNode(vt);
            }
            ConstructorArg ca = new ConstructorArg(vtresolved, a.IDENTIFIER() != null ? new Opt<>(new Name(a.IDENTIFIER().getText())) : new Opt<>());
            setASTNodePosition(a, ca);
            d.addConstructorArg(ca);
        }
        List<DataConstructor> l = new List<>();
        l.add(d);
        setV(ctx, new ExceptionDecl(ctx.n.getText(), v(ctx.annotations()), l));
    }

    @Override public void exitMain_block(RPLParser.Main_blockContext ctx) {
        setV(ctx, new MainBlock(v(ctx.annotations()), l(ctx.stmt())));
    }

    // Interfaces
    @Override public void exitInterface_decl(RPLParser.Interface_declContext ctx) {
        InterfaceDecl i = new InterfaceDecl(ctx.qualified_type_identifier().getText(), v(ctx.annotations()), l(ctx.e), l(ctx.methodsig()));
        setV(ctx, i);
    }

    @Override public void exitInterface_decl1(RPLParser.Interface_decl1Context ctx) {
        InterfaceDecl1 i = new InterfaceDecl1(ctx.qualified_type_identifier().getText(), v(ctx.annotations()), l(ctx.e), l(ctx.methodsig1()));
        setV(ctx, i);
    }

    @Override public void exitMethodsig(RPLParser.MethodsigContext ctx) {
        setV(ctx, new MethodSig(ctx.IDENTIFIER().getText(), v(ctx.type_use()), v(ctx.paramlist())));
    }

    @Override public void exitMethodsig1(RPLParser.Methodsig1Context ctx) {
        setV(ctx, new MethodSig1(ctx.IDENTIFIER().getText(), v(ctx.type_use()), v(ctx.p), v(ctx.q)));
    }

    // Classes
    @Override public void exitClass_decl(RPLParser.Class_declContext ctx) {
        ClassDecl c = setV(ctx, new ClassDecl(ctx.qualified_type_identifier().getText(), v(ctx.annotations()),
            new List<>(), l(ctx.interface_name()),
                                                         l(ctx.trait_usage()), new Opt<>(), l(ctx.casestmtbranch()), l(ctx.field_decl()), l(ctx.method())));
        if (ctx.paramlist() != null) {
            c.setParamList(v(ctx.paramlist()));
        }
        if (ctx.stmt() != null && !ctx.stmt().isEmpty()) {
            InitBlock b = new InitBlock(new List<>(), new List<>());
            for (RPLParser.StmtContext s : ctx.stmt()) {
                b.addStmt(v(s));
            }
            c.setInitBlock(b);
        }
    }

    @Override public void exitClass_decl1(RPLParser.Class_decl1Context ctx) {
        ClassDecl1 c = setV(ctx, new ClassDecl1(ctx.qualified_type_identifier().getText(), v(ctx.annotations()),
            new List<>(), l(ctx.interface_name()),
            l(ctx.trait_usage()), new Opt<>(), l(ctx.casestmtbranch()), l(ctx.field_decl()), l(ctx.method1())));
        if (ctx.paramlist() != null) {
            c.setParamList(v(ctx.paramlist()));
        }
        if (ctx.stmt() != null && !ctx.stmt().isEmpty()) {
            InitBlock b = new InitBlock(new List<>(), new List<>());
            for (RPLParser.StmtContext s : ctx.stmt()) {
                b.addStmt(v(s));
            }
            c.setInitBlock(b);
        }
    }

    @Override public void exitField_decl(RPLParser.Field_declContext ctx) {
        // FIXME: 'port' missing (for component model)
        FieldDecl f = setV(ctx, new FieldDecl(ctx.IDENTIFIER().getText(), v(ctx.type_use()), o(ctx.pure_exp())));
    }

    @Override public void exitMethod(RPLParser.MethodContext ctx) {
        MethodSig ms = new MethodSig(ctx.IDENTIFIER().getText(), v(ctx.type_use()), v(ctx.paramlist()));
        ms.setPosition(ctx.IDENTIFIER().getSymbol().getLine(), ctx.IDENTIFIER().getSymbol().getCharPositionInLine(),
                       ctx.paramlist().getStop().getLine(), ctx.paramlist().getStop().getCharPositionInLine() + ctx.paramlist().getStop().getText().length());
        Block b = new Block(new List<>(), new List<>());
        for (RPLParser.StmtContext s : ctx.stmt()) {
            b.addStmt(v(s));
        }
        setV(ctx, new MethodImpl(ms, b));
    }

    @Override public void exitMethod1(RPLParser.Method1Context ctx) {
        MethodSig1 ms = new MethodSig1(ctx.IDENTIFIER().getText(), v(ctx.type_use()), v(ctx.p), v(ctx.q));
        ms.setPosition(ctx.IDENTIFIER().getSymbol().getLine(), ctx.IDENTIFIER().getSymbol().getCharPositionInLine(),
            ctx.q.getStop().getLine(), ctx.q.getStop().getCharPositionInLine() + ctx.q.getStop().getText().length());
        Block b = new Block(new List<>(), new List<>());
        for (RPLParser.StmtContext s : ctx.stmt()) {
            b.addStmt(v(s));
        }
        setV(ctx, new MethodImpl1(ms, b));
    }

    // Statements
    @Override public void exitVardeclStmt(RPLParser.VardeclStmtContext ctx) {
        VarDecl v = new VarDecl(ctx.IDENTIFIER().getText(), v(ctx.type_exp()), new Opt<>());
        setASTNodePosition(ctx, v);
        if (ctx.exp() != null) {
            v.setInitExp(v(ctx.exp()));
        }
        setV(ctx, new VarDeclStmt(v(ctx.annotations()), v));
    }

    @Override public void exitAssignStmt(RPLParser.AssignStmtContext ctx) {
        setV(ctx, new AssignStmt(v(ctx.annotations()), v(ctx.var_or_field_ref()), v(ctx.exp())));
    }
    @Override public void exitSkipStmt(RPLParser.SkipStmtContext ctx) {
        setV(ctx, new SkipStmt(v(ctx.annotations())));
    }
    @Override public void exitReturnStmt(RPLParser.ReturnStmtContext ctx) {
        setV(ctx, new ReturnStmt(v(ctx.annotations()), v(ctx.exp())));
    }
    @Override public void exitAssertStmt(RPLParser.AssertStmtContext ctx) {
        setV(ctx, new AssertStmt(v(ctx.annotations()), v(ctx.exp())));
    }
    @Override public void exitBlockStmt(RPLParser.BlockStmtContext ctx) {
        setV(ctx, new Block(v(ctx.annotations()), l(ctx.stmt())));
    }
    @Override public void exitIfStmt(RPLParser.IfStmtContext ctx) {
        Stmt l = v(ctx.l);
        if (!(l instanceof Block)) {
            setV(ctx.l, new Block(new List<>(), new List<>(l)));
        }
        if (ctx.r != null) {
            Stmt r = v(ctx.r);
            if (!(r instanceof Block)) {
                setV(ctx.r, new Block(new List<>(), new List<>(r)));
            }
        }
        setV(ctx, new IfStmt(v(ctx.annotations()), v(ctx.c),
            v(ctx.l), o(ctx.r)));
    }
    @Override public void exitWhileStmt(RPLParser.WhileStmtContext ctx) {
        Stmt body = v(ctx.stmt());
        if (!(body instanceof Block)) {
            setV(ctx.stmt(), new Block(new List<>(), new List<>(body)));
        }
        setV(ctx, new WhileStmt(v(ctx.annotations()), v(ctx.c), v(ctx.stmt())));
    }
    @Override public void exitForeachStmt(RPLParser.ForeachStmtContext ctx) {
        Stmt body = v(ctx.stmt());
        if (!(body instanceof Block)) {
            setV(ctx.stmt(), new Block(new List<>(), new List<>(body)));
        }
        Opt<LoopVarDecl> indexvar;
        if (ctx.index == null) indexvar = new Opt<>();
        else indexvar = new Opt<>(new LoopVarDecl(ctx.index.getText()));
        setV(ctx, new ForeachStmt(v(ctx.annotations()), new LoopVarDecl(ctx.var.getText()), indexvar, v(ctx.l), v(ctx.stmt())));
    }
    @Override public void exitTryCatchFinallyStmt(RPLParser.TryCatchFinallyStmtContext ctx) {
        Stmt body = v(ctx.b);
        if (!(body instanceof Block)) {
            setV(ctx.b, new Block(new List<>(), new List<>(body)));
        }
        if (ctx.f != null) {
            Stmt finall = v(ctx.f);
            if (!(finall instanceof Block)) {
                setV(ctx.f, new Block(new List<>(), new List<>(finall)));
            }
        }
        setV(ctx, new TryCatchFinallyStmt(v(ctx.annotations()), v(ctx.b),
                                          l(ctx.casestmtbranch()), o(ctx.f)));
    }
    @Override public void exitAwaitStmt(RPLParser.AwaitStmtContext ctx) {
        setV(ctx, new AwaitStmt(v(ctx.annotations()), v(ctx.guard())));
    }
    @Override public void exitClaimGuard(RPLParser.ClaimGuardContext ctx) {
        setV(ctx, new ClaimGuard(v(ctx.var_or_field_ref())));
    }
    @Override public void exitDurationGuard(RPLParser.DurationGuardContext ctx) {
        if (ctx.max != null) {
            setV(ctx, new DurationGuard(v(ctx.min), v(ctx.max)));
        } else {
            setV(ctx, new DurationGuard(v(ctx.min), (PureExp)v(ctx.min).copy()));
        }
    }
    @Override public void exitExpGuard(RPLParser.ExpGuardContext ctx) {
        setV(ctx, new ExpGuard(v(ctx.e)));
    }
    @Override public void exitAndGuard(RPLParser.AndGuardContext ctx) {
        setV(ctx, new AndGuard(v(ctx.l), v(ctx.r)));
    }
    @Override public void exitSuspendStmt(RPLParser.SuspendStmtContext ctx) {
        setV(ctx, new SuspendStmt(v(ctx.annotations())));
    }
    @Override public void exitDurationStmt(RPLParser.DurationStmtContext ctx) {
        if (ctx.max != null) {
            setV(ctx, new DurationStmt(v(ctx.annotations()), v(ctx.min), v(ctx.max)));
        } else {
            setV(ctx, new DurationStmt(v(ctx.annotations()), v(ctx.min), (PureExp)v(ctx.min).copy()));
        }
    }

    @Override public void exitCostStmt(RPLParser.CostStmtContext ctx) {
            setV(ctx, new CostStmt(v(ctx.annotations()), v(ctx.c)));
    }

    @Override public void exitAddResStmt(RPLParser.AddResStmtContext ctx) {
        setV(ctx, new AddResStmt(v(ctx.annotations()), v(ctx.p)));
    }



    @Override public void exitReleaseResStmt(RPLParser.ReleaseResStmtContext ctx) {
        setV(ctx, new ReleaseResStmt(v(ctx.annotations()), v(ctx.p)));
    }

    @Override public void exitThrowStmt(RPLParser.ThrowStmtContext ctx) {
        setV(ctx, new ThrowStmt(v(ctx.annotations()), v(ctx.pure_exp())));
    }
    @Override public void exitDieStmt(RPLParser.DieStmtContext ctx) {
        setV(ctx, new DieStmt(v(ctx.annotations()), v(ctx.pure_exp())));
    }
    @Override public void exitMoveCogToStmt(RPLParser.MoveCogToStmtContext ctx) {
        setV(ctx, new MoveCogToStmt(v(ctx.annotations()), v(ctx.pure_exp())));
    }
    @Override public void exitExpStmt(RPLParser.ExpStmtContext ctx) {
        setV(ctx, new ExpressionStmt(v(ctx.annotations()), v(ctx.exp())));
    }
    @Override public void exitSwitchStmt(RPLParser.SwitchStmtContext ctx) {
        setV(ctx, new CaseStmt(v(ctx.annotations()), v(ctx.c), l(ctx.casestmtbranch())));
    }
    @Override public void exitCaseStmtOld(RPLParser.CaseStmtOldContext ctx) {
        setV(ctx, new CaseStmtOld(v(ctx.annotations()), v(ctx.c), l(ctx.casestmtbranch())));
    }
    @Override public void exitCasestmtbranch(RPLParser.CasestmtbranchContext ctx) {
        Stmt body = v(ctx.stmt());
        if (!(body instanceof Block)) {
            setV(ctx.stmt(), new Block(new List<>(), new List<>(body)));
        }
        setV(ctx, new CaseBranchStmt(v(ctx.pattern()), v(ctx.stmt())));
    }

    // Annotations
    @Override public void exitAnnotation(RPLParser.AnnotationContext ctx) {
        if (ctx.l == null) setV(ctx, new Annotation(v(ctx.r)));
        else setV(ctx, new TypedAnnotation(v(ctx.r), new UnresolvedTypeUse(ctx.l.getText(), new List<>())));
    }

    @Override public void exitAnnotations(RPLParser.AnnotationsContext ctx) {
        setV(ctx, l(ctx.al));
    }

    // Expressions
    @Override public void exitPureExp(RPLParser.PureExpContext ctx) {
        setV(ctx, v(ctx.pure_exp()));
    }
    @Override public void exitEffExp(RPLParser.EffExpContext ctx) {
        setV(ctx, v(ctx.eff_exp()));
    }

    // Side-effectful expressions
    @Override public void exitHoldExp(RPLParser.HoldExpContext ctx) {
        setV(ctx, new HoldExp(v(ctx.pure_exp_list())));
    }
    @Override public void exitGetExp(RPLParser.GetExpContext ctx) {
        setV(ctx, new GetExp(v(ctx.pure_exp())));
    }
    @Override public void exitNewExp(RPLParser.NewExpContext ctx) {
        NewExp n = setV(ctx, new NewExp(ctx.c.getText(), v(ctx.pure_exp_list()), new Opt<>()));
        if (ctx.l != null) { n.setLocal(new Local()); }
    }
    @Override public void exitAsyncCallExp(RPLParser.AsyncCallExpContext ctx) {
        if (ctx.a != null) {
            setV(ctx, new AwaitAsyncCall(v(ctx.o), ctx.m.getText(), v(ctx.pure_exp_list())));
        } else {
            setV(ctx, new AsyncCall(v(ctx.o), ctx.m.getText(), v(ctx.pure_exp_list())));
        }
    }

    @Override public void exitAsyncCall1Exp(RPLParser.AsyncCall1ExpContext ctx) {

            setV(ctx, new AsyncCall1Exp(v(ctx.o), ctx.m.getText(), v(ctx.o1), v(ctx.q), v(ctx.d)));

    }

    //@Override public void exitAsyncCallRRPLExp(RPLParser.AsyncCallRRPLExpContext ctx) {setV(ctx, new AsyncCall(v(ctx.o), ctx.m.getText(), v(ctx.pure_exp_list())));}
    @Override public void exitSyncCallExp(RPLParser.SyncCallExpContext ctx) {
        setV(ctx, new SyncCall(v(ctx.o), ctx.m.getText(), v(ctx.pure_exp_list())));
    }

    @Override public void exitSyncCall1Exp(RPLParser.SyncCall1ExpContext ctx) {
        setV(ctx, new SyncCall1Exp(v(ctx.o), ctx.m.getText(), v(ctx.o1),v(ctx.q), v(ctx.d)));
    }


   // @Override public void exitSyncCallRRPLExp(RPLParser.SyncCallRRPLExpContext ctx) {setV(ctx, new SyncCall(v(ctx.o), ctx.m.getText(), v(ctx.pure_exp_list())));}
    @Override public void exitOriginalCallExp(RPLParser.OriginalCallExpContext ctx) {
        List<PureExp> l = ctx.pure_exp_list() == null
            ? new List<>()
            : v(ctx.pure_exp_list());
        if (ctx.c != null) {
            setV(ctx, new TargetedOriginalCall(l, new DeltaID("core")));
        } else if (ctx.d != null) {
            setV(ctx, new TargetedOriginalCall(l, v(ctx.d)));
        } else {
            setV(ctx, new OriginalCall(l));
        }
    }

    @Override
    public void exitFunction_list(RPLParser.Function_listContext ctx) {
        List<ParFnAppParam> list = ctx.function_param() == null
            ? new List<>()
            : l(ctx.function_param());
        setV(ctx, list);
    }

    @Override
    public void exitFunction_param(RPLParser.Function_paramContext ctx) {
        if(ctx.anon_function_decl() != null) {
            setV(ctx, v(ctx.anon_function_decl()));
        } else if(ctx.function_name_param_decl() != null) {
            setV(ctx, v(ctx.function_name_param_decl()));
        }
    }

    @Override public void exitFunction_name_param_decl(RPLParser.Function_name_param_declContext ctx) {
        setV(ctx, new NamedParFnAppParam(ctx.IDENTIFIER().getText()));
    }

    @Override
    public void exitAnon_function_decl(RPLParser.Anon_function_declContext ctx) {
        List<ParamDecl> params = v(ctx.params);
        PureExp pureExp = v(ctx.pure_exp());
        setV(ctx, new AnonymousFunctionDecl(params, pureExp));
    }

    // Pure expressions
    @Override public void exitFunctionExp(RPLParser.FunctionExpContext ctx) {
        List<PureExp> l = ctx.pure_exp_list() == null
            ? new List<>()
            : v(ctx.pure_exp_list());
        setV(ctx, new FnApp(ctx.qualified_identifier().getText(), l));
    }
    @Override public void exitPartialFunctionExp(RPLParser.PartialFunctionExpContext ctx) {
        List<PureExp> params = ctx.pure_exp_list() == null
            ? new List<>()
            : v(ctx.pure_exp_list());
        List<ParFnAppParam> functionParams = ctx.function_list() == null
            ? new List<>()
            : v(ctx.function_list());

        setV(ctx, new ParFnApp(ctx.qualified_identifier().getText(), params, functionParams));
    }
    @Override public void exitVariadicFunctionExp(RPLParser.VariadicFunctionExpContext ctx) {
        List<PureExp> l = v(ctx.pure_exp_list());
        PureExp arglist = null;
        if (l.getNumChildNoTransform() == 0) {
            arglist = new DataConstructorExp("Nil", new List<>());
        } else {
            arglist = new ListLiteral(l);
        }
        setASTNodePosition(ctx.pure_exp_list(), arglist);
        List<PureExp> llist = new List<>();
        llist.add(arglist);
        setV(ctx, new FnApp(ctx.qualified_identifier().getText(), llist));
    }
    @Override public void exitConstructorExp(RPLParser.ConstructorExpContext ctx) {
        List<PureExp> l = ctx.pure_exp_list() == null
            ? new List<>()
            : v(ctx.pure_exp_list());
        setV(ctx,
                 new DataConstructorExp(ctx.qualified_type_identifier().getText(),
                                        l));
    }
    @Override public void exitUnaryExp(RPLParser.UnaryExpContext ctx) {
        switch (ctx.op.getType()) {
        case RPLParser.NEGATION :
            setV(ctx, new NegExp(v(ctx.pure_exp())));
            break;
        case RPLParser.MINUS :
            setV(ctx, new MinusExp(v(ctx.pure_exp())));
            break;
        }
    }
    @Override public void exitMultExp(RPLParser.MultExpContext ctx) {
        switch (ctx.op.getType()) {
        case RPLParser.MULT :
            setV(ctx, new MultMultExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.DIV :
            setV(ctx, new DivMultExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.MOD :
            setV(ctx, new ModMultExp(v(ctx.l), v(ctx.r)));
            break;
        }
    }
    @Override public void exitAddExp(RPLParser.AddExpContext ctx) {
        switch (ctx.op.getType()) {
        case RPLParser.PLUS :
            setV(ctx, new AddAddExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.MINUS :
            setV(ctx, new SubAddExp(v(ctx.l), v(ctx.r)));
            break;
        }
    }
    @Override public void exitGreaterExp(RPLParser.GreaterExpContext ctx) {
        switch (ctx.op.getType()) {
        case RPLParser.LT :
            setV(ctx, new LTExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.GT :
            setV(ctx, new GTExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.LTEQ :
            setV(ctx, new LTEQExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.GTEQ :
            setV(ctx, new GTEQExp(v(ctx.l), v(ctx.r)));
            break;
        }
    }
    @Override public void exitEqualExp(RPLParser.EqualExpContext ctx) {
        switch (ctx.op.getType()) {
        case RPLParser.EQEQ :
            setV(ctx, new EqExp(v(ctx.l), v(ctx.r)));
            break;
        case RPLParser.NOTEQ :
            setV(ctx, new NotEqExp(v(ctx.l), v(ctx.r)));
            break;
        }
    }
    @Override public void exitAndExp(RPLParser.AndExpContext ctx) {
        setV(ctx, new AndBoolExp(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitConjunction(RPLParser.ConjunctionContext ctx) {
        setV(ctx, new ConjunctionExp(v(ctx.p), v(ctx.q)));
    }

    @Override public void exitOrExp(RPLParser.OrExpContext ctx) {
        setV(ctx, new OrBoolExp(v(ctx.l), v(ctx.r)));
    }
    @Override public void exitVarOrFieldExp(RPLParser.VarOrFieldExpContext ctx) {
        setV(ctx, v(ctx.var_or_field_ref()));
    }
    @Override public void exitIntExp(RPLParser.IntExpContext ctx) {
        setV(ctx, new IntLiteral(ctx.INTLITERAL().getText()));
    }
    @Override public void exitFloatExp(RPLParser.FloatExpContext ctx) {
        setV(ctx, new FloatLiteral(ctx.FLOATLITERAL().getText()));
    }
    @Override public void exitStringExp(RPLParser.StringExpContext ctx) {
        setV(ctx, makeStringLiteral(ctx.STRINGLITERAL().getText()));
    }
    @Override public void exitTemplateStringExp(RPLParser.TemplateStringExpContext ctx) {
        setV(ctx, makeTemplateStringLiteral(ctx.TEMPLATESTRINGLITERAL().getText()));
    }
    @Override public void exitTemplateStringCompoundExp(RPLParser.TemplateStringCompoundExpContext ctx) {
        PureExp result = new AddAddExp(makeTemplateStringLiteral(ctx.TEMPLATESTRINGSTART().getText()),
                                       new FnApp("toString",
                                                 new List().add(v(ctx.e1))));
        for (int i = 0; i < ctx.e.size(); i++) {
            PureExp e = v(ctx.e.get(i));
            // List<PureExp> arglist = new List<PureExp>();
            // arglist.
            PureExp s = makeTemplateStringLiteral(ctx.b.get(i).getText());
            PureExp part = new AddAddExp(s,
                                         new FnApp("toString", new List().add(e)));
            result = new AddAddExp(result, part);
        }
        result = new AddAddExp(result, makeTemplateStringLiteral(ctx.TEMPLATESTRINGEND().getText()));
        setV(ctx, result);
    }
    @Override public void exitThisExp(RPLParser.ThisExpContext ctx) {
        setV(ctx, new ThisExp());
    }
    @Override public void exitDestinyExp(RPLParser.DestinyExpContext ctx) {
        setV(ctx, new DestinyExp());
    }
    @Override public void exitNullExp(RPLParser.NullExpContext ctx) {
        setV(ctx, new NullExp());
    }
    @Override public void exitWhenExp(RPLParser.WhenExpContext ctx) {
        setV(ctx, new IfExp(v(ctx.c),
            v(ctx.l),
            v(ctx.r)));
    }
    @Override public void exitIfExpOld(RPLParser.IfExpOldContext ctx) {
        setV(ctx, new IfExpOld(v(ctx.c),
            v(ctx.l),
            v(ctx.r)));
    }
    @Override public void exitCaseExp(RPLParser.CaseExpContext ctx) {
        List<CaseBranch> l = new List<>();
        for (RPLParser.CasebranchContext b : ctx.casebranch()) {
            l.add(v(b));
        }
        setV(ctx, new CaseExp(v(ctx.c), l));
    }
    @Override public void exitLetExp(RPLParser.LetExpContext ctx) {
	// For a source-level let expression with multiple bindings,
	// create nested let expressions with one binding each.

	// TODO: consider changing AST to support multiple bindings in
	// LetExp - backends might be able to do something useful with
	// the additional information
	int nbindings = ctx.e.size(); // ctx.t, ctx.id have the same length
	PureExp body = v(ctx.body);
	for (int i = nbindings - 1; i >= 0; i--) {
	    ParamDecl pd = new ParamDecl(ctx.id.get(i).getText(),
					 v(ctx.t.get(i)), new List<>());
	    setASTNodePosition(ctx.id.get(i), pd);
	    body = new LetExp(pd, v(ctx.e.get(i)), body);
	}
	setV(ctx, body);
    }
    @Override public void exitImplementsExp(RPLParser.ImplementsExpContext ctx) {
        setV(ctx, new ImplementsExp(v(ctx.e), v(ctx.i)));
    }
    @Override public void exitAsExp(RPLParser.AsExpContext ctx) {
        setV(ctx, new AsExp(v(ctx.e), v(ctx.i)));
    }
    @Override public void exitParenExp(RPLParser.ParenExpContext ctx) {
        setV(ctx, v(ctx.pure_exp()));
    }

    @Override public void exitCasebranch(RPLParser.CasebranchContext ctx) {
        setV(ctx, new CaseBranch(v(ctx.pattern()),
            v(ctx.pure_exp())));
    }

    @Override public void exitUnderscorePattern(RPLParser.UnderscorePatternContext ctx) {
        setV(ctx, new UnderscorePattern());
    }
    @Override public void exitIntPattern(RPLParser.IntPatternContext ctx) {
        setV(ctx, new LiteralPattern(new IntLiteral(ctx.INTLITERAL().getText())));
    }
    @Override public void exitStringPattern(RPLParser.StringPatternContext ctx) {
        setV(ctx, new LiteralPattern(makeStringLiteral(ctx.STRINGLITERAL().getText())));
    }
    @Override public void exitVarPattern(RPLParser.VarPatternContext ctx) {
        setV(ctx, new PatternVarUse(ctx.IDENTIFIER().getText()));
    }
    @Override public void exitConstructorPattern(RPLParser.ConstructorPatternContext ctx) {
        setV(ctx, new ConstructorPattern(ctx.qualified_type_identifier().getText(), l(ctx.pattern())));
    }

    @Override public void exitParamlist(RPLParser.ParamlistContext ctx) {
        setV(ctx, l(ctx.param_decl()));
    }

    @Override public void exitParam_decl(RPLParser.Param_declContext ctx) {
        setV(ctx, new ParamDecl(ctx.IDENTIFIER().getText(), v(ctx.type_exp()), v(ctx.annotations())));
    }

    @Override public void exitFunction_name_list(RPLParser.Function_name_listContext ctx) {
        setV(ctx, l(ctx.function_name_decl()));
    }

    @Override public void exitFunction_name_decl(RPLParser.Function_name_declContext ctx) {
        setV(ctx, new FunctionParamDecl(ctx.IDENTIFIER().getText()));
    }

    @Override public void exitInterface_name(RPLParser.Interface_nameContext ctx) {
        setV(ctx, new InterfaceTypeUse(ctx.qualified_type_identifier().getText(), new List<>()));
    }

    @Override public void exitPure_exp_list(RPLParser.Pure_exp_listContext ctx) {
        List<PureExp> l = setV(ctx, new List<PureExp>());
        for (RPLParser.Pure_expContext a : ctx.pure_exp()) {
            l.add(v(a));
        }
    }

    @Override public void exitType_use(RPLParser.Type_useContext ctx) {
        /* As we could be looking at an interface type, first keep symbol
         * unresolved and have rewrite-rules patch it up.
         * However, this means that in the parser the DataConstructor could
         * be seeing. But there we know what it must be and "rewrite" it ourselves.
         */
        if (ctx.p.isEmpty()) {
            // normal type use
            setV(ctx, new UnresolvedTypeUse(ctx.n.getText(), v(ctx.annotations())));
        } else {
            // parametric type use
            ParametricDataTypeUse p
                = setV(ctx, new ParametricDataTypeUse(ctx.n.getText(), v(ctx.annotations()),
                new List<>()));
            for (RPLParser.Type_useContext c : ctx.type_use()) {
                p.addParam(v(c));
            }
        }
    }

    @Override
    public void exitType_use_paramlist(RPLParser.Type_use_paramlistContext ctx) {
        List<TypeUse> list = setV(ctx, new List<TypeUse>());
        for (RPLParser.Type_useContext typeUseContext : ctx.type_use()) {
            list.add(v(typeUseContext));
        }
    }

    @Override public void exitType_exp(RPLParser.Type_expContext ctx) {
        if (ctx.p.isEmpty()) {
            // normal type use
            setV(ctx, new UnresolvedTypeUse(ctx.n.getText(), new List<>()));
        } else {
            // parametric type use
            ParametricDataTypeUse p
                = setV(ctx, new ParametricDataTypeUse(ctx.n.getText(), new List<>(),
                new List<>()));
            for (RPLParser.Type_useContext c : ctx.type_use()) {
                p.addParam(v(c));
            }
        }
    }

    @Override public void exitVar_or_field_ref(RPLParser.Var_or_field_refContext ctx) {
        if (ctx.getChildCount() == 1) { // id
            setV(ctx, new VarUse(ctx.IDENTIFIER().getText()));
        } else {                // this.id
            setV(ctx, new FieldUse(ctx.IDENTIFIER().getText()));
        }
    }

    @Override public void exitQualified_type_identifier(RPLParser.Qualified_type_identifierContext ctx) {
        setV(ctx, new Name(ctx.getText()));
    }

    @Override public void exitQualified_identifier(RPLParser.Qualified_identifierContext ctx) {
        setV(ctx, new Name(ctx.getText()));
    }

    @Override public void exitAny_identifier(RPLParser.Any_identifierContext ctx) {
        setV(ctx, v(ctx.getChild(0))); // relies on any_identifier
                                       // having one token
    }

    // Deltas
    @Override public void exitDelta_decl(RPLParser.Delta_declContext ctx) {
        setV(ctx, new DeltaDecl(ctx.TYPE_IDENTIFIER().getText(),
                                l(ctx.p), o(ctx.delta_used_module()),
                                l(ctx.module_modifier())));
    }

    @Override public void exitDeltaFieldParam(RPLParser.DeltaFieldParamContext ctx) {
        setV(ctx, new DeltaFieldParam(v(ctx.param_decl())));
    }

    @Override public void exitDeltaClassParam(RPLParser.DeltaClassParamContext ctx) {
        setV(ctx, new DeltaClassParam(ctx.qualified_type_identifier().getText(),
            v(ctx.has_condition())));
    }

    @Override public void exitDeltaHasFieldCondition(RPLParser.DeltaHasFieldConditionContext ctx) {
        setV(ctx, new HasField(v(ctx.f)));
    }

    @Override public void exitDeltaHasMethodCondition(RPLParser.DeltaHasMethodConditionContext ctx) {
        setV(ctx, new HasMethod(v(ctx.m)));
    }

    @Override public void exitDeltaHasInterfaceCondition(RPLParser.DeltaHasInterfaceConditionContext ctx) {
        setV(ctx, new HasInterface(v(ctx.i)));
    }

    @Override public void exitDelta_used_module(RPLParser.Delta_used_moduleContext ctx) {
        setV(ctx, new DeltaAccess(ctx.qualified_type_identifier().getText()));
    }

    @Override public void exitModule_modifier(RPLParser.Module_modifierContext ctx) {
        setV(ctx, v(ctx.getChild(0))); // relies on node having one token
    }

    @Override public void exitDeltaAddFunctionModifier(RPLParser.DeltaAddFunctionModifierContext ctx) {
        setV(ctx, new AddFunctionModifier(v(ctx.function_decl())));
    }

    @Override public void exitDeltaAddDataTypeModifier(RPLParser.DeltaAddDataTypeModifierContext ctx) {
        setV(ctx, new AddDataTypeModifier(v(ctx.datatype_decl())));
    }

    @Override public void exitDeltaAddTypeSynModifier(RPLParser.DeltaAddTypeSynModifierContext ctx) {
        setV(ctx, new AddTypeSynModifier(v(ctx.typesyn_decl())));
    }

    @Override public void exitDeltaModifyTypeSynModifier(RPLParser.DeltaModifyTypeSynModifierContext ctx) {
        setV(ctx, new ModifyTypeSynModifier(v(ctx.typesyn_decl())));
    }

    @Override public void exitDeltaModifyDataTypeModifier(RPLParser.DeltaModifyDataTypeModifierContext ctx) {
        setV(ctx, new ModifyDataTypeModifier(v(ctx.datatype_decl())));
    }

    @Override public void exitDeltaAddClassModifier(RPLParser.DeltaAddClassModifierContext ctx) {
        setV(ctx, new AddClassModifier(v(ctx.class_decl())));
    }

    @Override public void exitDeltaRemoveClassModifier(RPLParser.DeltaRemoveClassModifierContext ctx) {
        setV(ctx, new RemoveClassModifier(ctx.qualified_type_identifier().getText()));
    }

    @Override public void exitDeltaModifyClassModifier(RPLParser.DeltaModifyClassModifierContext ctx) {
        setV(ctx, new ModifyClassModifier(ctx.n.getText(), l(ctx.ia), l(ctx.ir),
                                          l(ctx.class_modifier_fragment())));
    }

    @Override public void exitDeltaAddInterfaceModifier(RPLParser.DeltaAddInterfaceModifierContext ctx) {
        setV(ctx, new AddInterfaceModifier(v(ctx.interface_decl())));
    }

    @Override public void exitDeltaRemoveInterfaceModifier(RPLParser.DeltaRemoveInterfaceModifierContext ctx) {
        setV(ctx, new RemoveInterfaceModifier(ctx.qualified_type_identifier().getText()));
    }

    @Override public void exitDeltaModifyInterfaceModifier(RPLParser.DeltaModifyInterfaceModifierContext ctx) {
        setV(ctx, new ModifyInterfaceModifier(ctx.qualified_type_identifier().getText(),
                                              l(ctx.interface_modifier_fragment())));
    }

    @Override public void exitDeltaAddFieldFragment(RPLParser.DeltaAddFieldFragmentContext ctx) {
        setV(ctx, new AddFieldModifier(v(ctx.field_decl())));
    }
    @Override public void exitDeltaRemoveFieldFragment(RPLParser.DeltaRemoveFieldFragmentContext ctx) {
        setV(ctx, new RemoveFieldModifier(v(ctx.field_decl())));
    }

    @Override public void exitDeltaAddMethodsigFragment(RPLParser.DeltaAddMethodsigFragmentContext ctx) {
        setV(ctx, new AddMethodSigModifier(v(ctx.methodsig())));
    }
    @Override public void exitDeltaRemoveMethodsigFragment(RPLParser.DeltaRemoveMethodsigFragmentContext ctx) {
        setV(ctx, new RemoveMethodSigModifier(v(ctx.methodsig())));
    }

    @Override public void exitDeltaAddModuleImportFragment(RPLParser.DeltaAddModuleImportFragmentContext ctx) {
        setV(ctx, new AddImportModifier(v(ctx.module_import())));
    }
    @Override public void exitDeltaAddModuleExportFragment(RPLParser.DeltaAddModuleExportFragmentContext ctx) {
        setV(ctx, new AddExportModifier(v(ctx.module_export())));
    }

    // Productline
    @Override public void exitProductline_decl(RPLParser.Productline_declContext ctx) {
        setV(ctx, new ProductLine(ctx.TYPE_IDENTIFIER().getText(),
                                  l(ctx.feature()), l(ctx.delta_clause())));
    }

    @Override public void exitFeature(RPLParser.FeatureContext ctx) {
        setV(ctx, new Feature((ctx.p == null ? "" : "$") + ctx.TYPE_IDENTIFIER().getText(),
                              l(ctx.attr_assignment())));
    }

    @Override public void exitAttr_assignment(RPLParser.Attr_assignmentContext ctx) {
        Value val = null;
        String id = ctx.IDENTIFIER().getText();
        if (ctx.i != null) val = new IntVal(Integer.parseInt(ctx.i.getText()));
        else if(ctx.s != null) val = new StringVal(ctx.s.getText());
        else val = new UnknownVal(ctx.b.getText());
        setV(ctx, new AttrAssignment(id, val));
    }

    @Override public void exitDelta_clause(RPLParser.Delta_clauseContext ctx) {
        setV(ctx, new DeltaClause(v(ctx.deltaspec()),
                                  ctx.after_condition() == null ? new List()
                                  : v(ctx.after_condition()),
                                  o(ctx.from_condition()), o(ctx.when_condition())));
    }

    @Override public void exitDeltaspec(RPLParser.DeltaspecContext ctx) {
        setV(ctx, new Deltaspec(ctx.TYPE_IDENTIFIER().getText(), l(ctx.deltaspec_param())));
    }

    @Override public void exitBoolOrIDDeltaspecParam(RPLParser.BoolOrIDDeltaspecParamContext ctx) {
        String id = ctx.TYPE_IDENTIFIER().getText();
        setV(ctx, id.equals("True")
             ? new Const(new BoolVal(true))
             : (id.equals("False") ? new Const(new BoolVal(false)) : new FID(id)));
    }
    @Override public void exitIntDeltaspecParam(RPLParser.IntDeltaspecParamContext ctx) {
        setV(ctx, new Const(new IntVal(Integer.parseInt(ctx.INTLITERAL().getText()))));
    }
    @Override public void exitFIDAIDDeltaspecParam(RPLParser.FIDAIDDeltaspecParamContext ctx) {
        setV(ctx, new FIDAID(ctx.TYPE_IDENTIFIER().getText(), ctx.IDENTIFIER().getText()));
    }

    @Override public void exitAfter_condition(RPLParser.After_conditionContext ctx) {
        setV(ctx, l(ctx.delta_id()));
    }

    @Override public void exitDelta_id(RPLParser.Delta_idContext ctx) {
        setV(ctx, new DeltaID(ctx.TYPE_IDENTIFIER().getText()));
    }

    @Override public void exitFrom_condition(RPLParser.From_conditionContext ctx) {
        setV(ctx, v(ctx.application_condition()));
    }

    @Override public void exitWhen_condition(RPLParser.When_conditionContext ctx) {
        setV(ctx, v(ctx.application_condition()));
    }

    @Override public void exitNotApplicationCondition(RPLParser.NotApplicationConditionContext ctx) {
        setV(ctx, new AppCondNot(v(ctx.application_condition())));
    }

    @Override public void exitAndApplicationCondition(RPLParser.AndApplicationConditionContext ctx) {
        setV(ctx, new AppCondAnd(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitOrApplicationCondition(RPLParser.OrApplicationConditionContext ctx) {
        setV(ctx, new AppCondOr(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitParenApplicationCondition(RPLParser.ParenApplicationConditionContext ctx) {
        setV(ctx, v(ctx.application_condition()));
    }

    @Override public void exitFeatureApplicationCondition(RPLParser.FeatureApplicationConditionContext ctx) {
        setV(ctx, new AppCondFeature(((Feature)v(ctx.feature())).getName()));
    }

    // Products
    @Override public void exitProduct_decl(RPLParser.Product_declContext ctx) {
        if(ctx.product_expr() == null) {
            // old syntax: a product is declared as a set of features
            setV(ctx, new ProductDecl(ctx.TYPE_IDENTIFIER().getText(), new ProductFeatureSet(l(ctx.feature()))));
        } else {
            // new syntax: using product expressions
            setV(ctx, new ProductDecl(ctx.TYPE_IDENTIFIER().getText(), v(ctx.product_expr())));
        }
    }

    // Product Expression
    @Override public void exitProductFeatureSet(RPLParser.ProductFeatureSetContext ctx) {
        setV(ctx, new ProductFeatureSet(l(ctx.feature())));
    }

    @Override public void exitProductIntersect(RPLParser.ProductIntersectContext ctx) {
        setV(ctx, new ProductIntersect(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitProductUnion(RPLParser.ProductUnionContext ctx) {
        setV(ctx, new ProductUnion(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitProductDifference(RPLParser.ProductDifferenceContext ctx) {
        setV(ctx, new ProductDifference(v(ctx.l), v(ctx.r)));
    }

    @Override public void exitProductName(RPLParser.ProductNameContext ctx) {
        setV(ctx, new ProductName(ctx.TYPE_IDENTIFIER().getText()));
    }

    @Override public void exitProductParen(RPLParser.ProductParenContext ctx) {
        setV(ctx, v(ctx.product_expr()));
    }

    //  mTVL
	@Override public void exitFextension(RPLParser.FextensionContext ctx) {
        setV(ctx, new FExt(ctx.TYPE_IDENTIFIER().getText(),
                           o(ctx.feature_decl_group()),
                           new AttrConstraints(l(ctx.feature_decl_attribute()),
                                                      l(ctx.feature_decl_constraint()))));
    }

    @Override public void exitFeature_decl(RPLParser.Feature_declContext ctx) {
        setV(ctx, new FeatureDecl(ctx.TYPE_IDENTIFIER().getText(),
                                  o(ctx.feature_decl_group()),
                                  new AttrConstraints(l(ctx.feature_decl_attribute()),
                                                      l(ctx.feature_decl_constraint()))));
    }

    @Override public void exitFeature_decl_group(RPLParser.Feature_decl_groupContext ctx) {
        Cardinality c = null;
        if (ctx.o != null) c = new CRange(1,1);
        else if (ctx.a != null) c = new AllOf();
        else if (ctx.s != null) c = new Minim(Integer.parseInt(ctx.l.getText()));
        else c = new CRange(Integer.parseInt(ctx.l.getText()),
                            Integer.parseInt(ctx.u.getText()));
        setV(ctx, new Group(c, l(ctx.fnode())));
    }

    @Override public void exitFnode(RPLParser.FnodeContext ctx) {
        if (ctx.o == null) setV(ctx, new MandFeat(v(ctx.feature_decl())));
        else setV(ctx, new OptFeat(v(ctx.feature_decl())));
    }


	@Override public void exitFeatureDeclConstraintIfIn(RPLParser.FeatureDeclConstraintIfInContext ctx) {
        setV(ctx, new IfIn(v(ctx.mexp())));
    }
	@Override public void exitFeatureDeclConstraintIfOut(RPLParser.FeatureDeclConstraintIfOutContext ctx) {
        setV(ctx, new IfOut(v(ctx.mexp())));
    }
	@Override public void exitFeatureDeclConstraintExclude(RPLParser.FeatureDeclConstraintExcludeContext ctx) {
        setV(ctx, new Exclude(new FeatVar(ctx.TYPE_IDENTIFIER().getText())));
    }
	@Override public void exitFeatureDeclConstraintRequire(RPLParser.FeatureDeclConstraintRequireContext ctx) {
        setV(ctx, new Require(new FeatVar(ctx.TYPE_IDENTIFIER().getText())));
    }

	@Override public void exitMexp(RPLParser.MexpContext ctx) {
        if (ctx.op != null) {
            switch (ctx.op.getType()) {
            case RPLParser.OROR :
                setV(ctx, new MOrBoolExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.ANDAND :
                setV(ctx, new MAndBoolExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.IMPLIES :
                setV(ctx, new MImpliesExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.EQUIV :
                setV(ctx, new MEquivExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.EQEQ :
                setV(ctx, new MEqExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.NOTEQ :
                setV(ctx, new MNotEqExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.LT :
                setV(ctx, new MLTExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.GT :
                setV(ctx, new MGTExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.LTEQ :
                setV(ctx, new MLTEQExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.GTEQ :
                setV(ctx, new MGTEQExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.PLUS :
                setV(ctx, new MAddAddExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.MINUS :
                if (ctx.l != null) setV(ctx, new MSubAddExp(v(ctx.l), v(ctx.r)));
                else setV(ctx, new MMinusExp(v(ctx.a)));
                return;
            case RPLParser.MULT :
                setV(ctx, new MMultMultExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.DIV :
                setV(ctx, new MDivMultExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.MOD :
                setV(ctx, new MModMultExp(v(ctx.l), v(ctx.r)));
                return;
            case RPLParser.NEGATION :
                setV(ctx, new MNegExp(v(ctx.a)));
                return;
            }
        } else if (ctx.a != null)
            setV(ctx, v(ctx.a));
        else if (ctx.INTLITERAL() != null)
            setV(ctx, new MValue(new IntVal(Integer.parseInt(ctx.INTLITERAL().getText()))));
        else if (ctx.IDENTIFIER() != null) {
            if (ctx.TYPE_IDENTIFIER() != null)
                setV(ctx, new FAVar(ctx.TYPE_IDENTIFIER().getText(),
                                    ctx.IDENTIFIER().getText()));
            else
                setV(ctx, new AttVar(ctx.IDENTIFIER().getText()));
        } else {
            // TYPE_IDENTIFIER is not null
            String id = ctx.TYPE_IDENTIFIER().getText();
            if (id.equals("True")) setV(ctx, new MValue(new BoolVal(true)));
            else if (id.equals("False")) setV(ctx, new MValue(new BoolVal(false)));
            else setV(ctx, new FeatVar(id));
        }
    }


    @Override public void exitFeature_decl_attribute(RPLParser.Feature_decl_attributeContext ctx) {
        String t = ctx.TYPE_IDENTIFIER().getText();
        if (ctx.l != null) {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new IntMType(t, v(ctx.l), v(ctx.u))));
        } else if (ctx.is != null && !ctx.is.isEmpty()) {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new IntListMType(t, l(ctx.is))));
        } else if (t.equals("Int")) {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new IntMType(t, new Limit(), new Limit())));
        } else if (t.equals("String")) {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new StringMType(t)));
        } else if (t.equals("Bool")) {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new BoolMType(t)));
        } else {
            setV(ctx, new Attribute(ctx.IDENTIFIER().getText(), new UnresolvedMType(t)));
        }
    }

	@Override public void exitBoundary_int(RPLParser.Boundary_intContext ctx) {
        if (ctx.star != null) setV(ctx, new Limit());
        else setV(ctx, v(ctx.boundary_val()));
    }
	@Override public void exitBoundary_val(RPLParser.Boundary_valContext ctx) {
        setV(ctx, new BoundaryVal((ctx.m == null ? +1 : -1) * Integer.parseInt(ctx.INTLITERAL().getText())));
    }

}

