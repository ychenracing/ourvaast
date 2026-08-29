# ChatGPT Project Brief

> 本文件只保存长期稳定、仓库级的信息。当前任务、临时分支、SHA、测试状态和执行进度应保存在当前 Pull Request 正文中。

## 1. Project

- 项目名称：ourvaast
- GitHub 仓库：`ychenracing/ourvaast`
- 默认分支：`master`
- 系统定位：保存 VAAST 相关变异注释工具、辅助脚本和早期 Java 工程代码的研究型仓库。
- 项目目标：为 VAAST 工作流所需的变异格式整理、转换和运行材料提供可追踪的代码与资源。

## 2. Purpose and Non-Goals

仓库围绕 VAAST 工作保存 Java 源码、Python 与 shell 辅助脚本，以及 VAAST、VAT、VST 相关资源。README 将项目描述为团队的 VAAST 工作，并记录一键运行与 VAAST 运行代码的演进。

仓库文档未把本项目定义为通用变异分析平台、托管服务或生产级临床系统；不要在缺少权威文档和验证的情况下推断这些能力。

## 3. Architecture and Module Boundaries

- `src/` 是 Eclipse Java 工程的源代码根目录。
- `src/META-INF/` 保存 Java 工程元数据。
- `src/cn/` 保存 Java 包层次。
- `vaast/` 保存 VAAST 相关辅助资源。
- `vaast/SortGvf.py` 负责 GVF 数据整理的 Python 辅助处理。
- `vaast/convertVcfToGvf.sh` 是 VCF 到 GVF 的转换辅助脚本。
- `vaast/VAAST/`、`vaast/VAT/` 和 `vaast/VST/` 保存对应工具或流程材料。
- `.classpath` 和 `.project` 定义 Eclipse 工程边界；`.classpath` 指向 `src/`、JavaSE-1.6 容器和 `bin/` 输出。

现有文档未给出更细的稳定组件 Owner、数据流或部署架构。修改前应从目标代码和调用链核实边界，不得凭目录名补写架构事实。

## 4. Non-Negotiable Constraints

- 保持输入、输出格式和脚本调用约定的兼容性，除非任务明确要求并有相应验证。
- 不得把研究脚本或历史工具材料描述成已验证的临床诊断能力。
- 不得在仓库中提交受限数据、个人基因组数据、凭证或本机绝对路径。
- 对 Java、Python、shell 或随附工具版本的要求必须以仓库文件或上游文档为依据。
- 文档、元数据和治理改动不应改变 VAAST 运行、变异转换或排序行为。
- 遵循 `AGENTS.md` 的渐进式、按影响范围验证策略。

## 5. Authoritative Sources

- 项目定位和历史说明：`README.md`
- 工程协作与验证约定：`AGENTS.md`
- Eclipse Java 工程配置：`.classpath`、`.project`
- Java 实现：`src/`
- VAAST 辅助脚本与资源：`vaast/`
- 具体脚本接口：对应脚本自身及其直接调用者
- 版本与发布：Git 历史、标签和 GitHub Releases（如适用）

当不同来源冲突时，以目标实现、直接调用链和可复现验证为准，并在 Pull Request 中说明冲突。

## 6. Standard Commands

仓库文档未明确安装、构建、测试、lint 或完整运行命令。不要凭 Eclipse、Java、Python 或 shell 文件推测标准命令。

处理具体任务时，应先读取目标脚本、直接调用者和相关工具说明；只在 Pull Request 中记录实际核验并执行过的命令。纯文档治理变更可进行静态 diff 与内容检查。

## 7. Important Paths

- `README.md`：项目定位和历史说明。
- `AGENTS.md`：渐进式验证约定。
- `.classpath`、`.project`：Eclipse Java 工程配置。
- `src/`：Java 源代码和元数据。
- `vaast/`：VAAST、VAT、VST 资源及格式处理脚本。
- `vaast/SortGvf.py`：GVF 排序辅助脚本。
- `vaast/convertVcfToGvf.sh`：VCF 到 GVF 转换辅助脚本。

## 8. CI and Acceptance Entry Points

仓库未提供已文档化的 CI workflow 或统一自动化验收入口。

- 实现改动应依据 `AGENTS.md` 从最小受影响范围开始验证。
- 对脚本或格式处理的改动，应使用可公开、可最小化的样例验证输入输出，不得提交敏感基因组数据。
- 最终验收应准确记录已执行检查、未执行检查和仍未知事项。
- Definition of Done：验收条件满足；变更范围与预期一致；没有未解决的阻断审查；验证证据没有被夸大或伪造。

## 9. Prohibited Actions

- 不得提交凭证、受限数据、个人基因组数据或本地绝对路径。
- 不得凭文件扩展名猜测并宣称构建、测试或运行命令有效。
- 不得把未执行的工具链、样例或研究流程报告为已验证。
- 不得擅自改写 Git 历史或 force push。
- 不得丢弃未知工作或覆盖无关改动。
- 不得让治理文档成为与实现相竞争的运行时真相。
- 不得根据旧聊天猜测当前分支、SHA、PR 或 CI 状态。

## 10. Context Loading Protocol

1. 新开发任务可以直接使用自然语言提出，不要求预先填写固定 Prompt。
2. 开始任务时先读取本文件。
3. 搜索与任务相关的开放 PR、分支和 Issue。
4. 如果存在匹配工作，从现有现场原地继续。
5. 当前动态任务状态默认维护在 Pull Request 正文。
6. 不强制普通单 PR 任务创建 Issue。
7. 优先读取目标代码、直接调用者、相关测试和直接相关配置。
8. 只有证据不足、状态冲突或影响范围扩大时才扩大读取。
9. 不默认加载完整仓库、完整聊天、完整日志或全部 GitHub Actions 历史。
10. 长对话交接使用 `conversation-continuity-guard`，但 GitHub 当前现场仍是状态权威来源。

## 11. References

- `README.md`
- `AGENTS.md`
- `.classpath`
- `.project`
- `src/`
- `vaast/`
