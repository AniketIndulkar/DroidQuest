const fs = require('fs');
const path = require('path');

const lessonsDir = path.join(__dirname, '..', 'content', 'lessons');
let changed = 0;

for (const name of fs.readdirSync(lessonsDir).filter((it) => it.endsWith('.json')).sort()) {
  const file = path.join(lessonsDir, name);
  const lesson = JSON.parse(fs.readFileSync(file, 'utf8'));
  let lessonChanged = false;
  lesson.revealStages.recall = lesson.revealStages.recall.map((recall, index) => {
    if (recall.id) return recall;
    lessonChanged = true;
    return { id: `${lesson.id}-recall-${index + 1}`, ...recall };
  });
  if (lessonChanged) {
    fs.writeFileSync(file, `${JSON.stringify(lesson, null, 2)}\n`);
    changed += 1;
  }
}

console.log(`Added stable recall IDs to ${changed} lesson files.`);
