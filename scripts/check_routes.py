"""Check view files, literal local URLs, and missing links to implemented modules.
Run from the repository root: python scripts/check_routes.py
This is a source check; run Maven tests for actual rendering and CRUD behavior.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
controllers = root / 'src/main/java/com/oop/fooddelivery/controller'
templates = root / 'src/main/resources/templates'
static = root / 'src/main/resources/static'
errors = []
routes = set()
for file in controllers.glob('*.java'):
    text = file.read_text()
    match = re.search(r'@RequestMapping\("([^\"]+)"\)', text)
    prefix = match[1] if match else ''
    for match in re.finditer(r'@(?:Get|Post)Mapping(?:\("([^\"]+)"\))?', text):
        routes.add(prefix + (match[1] or ''))
    for view in re.findall(r'return "([^\"]+)";', text):
        if not view.startswith('redirect:') and not (templates / (view + '.html')).is_file():
            errors.append(f'{file.name}: missing template {view}.html')
for file in templates.rglob('*.html'):
    text = file.read_text()
    for attr, url in re.findall(r'(?<![\w:])(href|src|action)="(/[^\"]*)"', text):
        if url not in routes and not (static / url.lstrip('/')).is_file():
            errors.append(f'{file.relative_to(root)}: unmatched {attr} {url}')
        if f'th:{attr}="@{{{url}}}"' not in text:
            errors.append(f'{file.relative_to(root)}: URL lacks context-path handling: {url}')
    for url in re.findall(r'th:(?:href|src|action)="@\{(/[^\"]+)\}"', text):
        path = url.split('(', 1)[0]
        if path not in routes and not (static / path.lstrip('/')).is_file():
            errors.append(f'{file.relative_to(root)}: unmatched Thymeleaf URL {path}')
    for icon in ('fa-motorcycle', 'fa-store', 'fa-burger'):
        if re.search(r'href="#"[^>]*>\s*<i class="fa-solid ' + icon, text):
            errors.append(f'{file.relative_to(root)}: implemented module still uses # ({icon})')
if errors:
    raise SystemExit('\n'.join(errors))
print(f'PASS: {len(routes)} controller routes; all returned views exist; literal local URLs resolve; implemented sidebar links are connected.')
