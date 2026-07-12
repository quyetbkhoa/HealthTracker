import os
import re

source_dir = r"x:\AndroidStudioProject\HealthTracker\app\src\main\java\com\quyetbkhoa\healthtracker"
output_file = r"x:\AndroidStudioProject\HealthTracker\app_structure.md"

class_pattern = re.compile(r"^(?:(?:public|private|protected|internal|abstract|sealed|open|data)\s+)*(class|interface|object|enum class)\s+([A-Za-z0-9_]+)")
func_pattern = re.compile(r"^(?:(?:public|private|protected|internal|override|suspend|inline)\s+)*(fun)\s+([A-Za-z0-9_]+)\s*\(")
composable_pattern = re.compile(r"^\s*@Composable\s*fun\s+([A-Za-z0-9_]+)\s*\(")

with open(output_file, "w", encoding="utf-8") as out:
    out.write("# Chi tiết Cấu trúc ứng dụng HealthTracker\n\n")
    
    for root, dirs, files in os.walk(source_dir):
        for file in files:
            if file.endswith(".kt"):
                filepath = os.path.join(root, file)
                relpath = os.path.relpath(filepath, source_dir)
                
                with open(filepath, "r", encoding="utf-8") as f:
                    content = f.readlines()
                
                has_printed_file = False
                
                for i, line in enumerate(content):
                    line_stripped = line.strip()
                    
                    # Check class
                    match_class = class_pattern.match(line_stripped)
                    if match_class:
                        if not has_printed_file:
                            out.write(f"## File: {relpath}\n")
                            has_printed_file = True
                        out.write(f"- **{match_class.group(1)} {match_class.group(2)}**\n")
                        continue
                    
                    # Check composable on same line
                    match_comp = composable_pattern.match(line_stripped)
                    if match_comp:
                        if not has_printed_file:
                            out.write(f"## File: {relpath}\n")
                            has_printed_file = True
                        out.write(f"  - Composable: `{match_comp.group(1)}()`\n")
                        continue
                        
                    # Check function (might be composable from previous line)
                    match_fun = func_pattern.match(line_stripped)
                    if match_fun:
                        if i > 0 and "@Composable" in content[i-1]:
                            if not has_printed_file:
                                out.write(f"## File: {relpath}\n")
                                has_printed_file = True
                            out.write(f"  - Composable: `{match_fun.group(2)}()`\n")
                        else:
                            if not has_printed_file:
                                out.write(f"## File: {relpath}\n")
                                has_printed_file = True
                            out.write(f"  - Hàm: `{match_fun.group(2)}()`\n")
                        continue
                        
                if has_printed_file:
                    out.write("\n")

print("Done")
