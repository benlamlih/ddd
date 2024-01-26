import {Component, ElementRef, OnDestroy, OnInit, Renderer2, ViewEncapsulation} from '@angular/core';
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-animation',
  standalone: true,
  imports: [
    RouterLink
  ],
  templateUrl: './animation.component.html',
  styleUrl: './animation.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class AnimationComponent implements OnInit, OnDestroy {
  private scriptElement: HTMLScriptElement | null = null;

  constructor(private renderer: Renderer2, private el: ElementRef) {
  }

  ngOnInit() {
    this.removeExistingScript();
    const script = this.renderer.createElement('script');
    script.type = 'text/javascript';
    script.text = `
    (function() {
    let elts = {
      text1: document.getElementById("text1"),
      text2: document.getElementById("text2")
    };

    let texts = [
      "Boundless",
      "Beds"
    ];

    let morphTime = 1;
    let cooldownTime = 0.25;

    let textIndex = texts.length - 1;
    let time = new Date();
    let morph = 0;
    let cooldown = cooldownTime;

    elts.text1.textContent = texts[textIndex % texts.length];
    elts.text2.textContent = texts[(textIndex + 1) % texts.length];

    function doMorph() {
      morph -= cooldown;
      cooldown = 0;

      let fraction = morph / morphTime;

      if (fraction > 1) {
        cooldown = cooldownTime;
        fraction = 1;
      }

      setMorph(fraction);
    }

    function setMorph(fraction) {
      elts.text2.style.filter = \`blur(\${Math.min(8 / fraction - 8, 100)}px)\`;
      elts.text2.style.opacity = \`\${Math.pow(fraction, 0.4) * 100}%\`;

      fraction = 1 - fraction;
      elts.text1.style.filter = \`blur(\${Math.min(8 / fraction - 8, 100)}px)\`;
      elts.text1.style.opacity = \`\${Math.pow(fraction, 0.4) * 100}%\`;

      elts.text1.textContent = texts[textIndex % texts.length];
      elts.text2.textContent = texts[(textIndex + 1) % texts.length];
    }

    function doCooldown() {
      morph = 0;

      elts.text2.style.filter = "";
      elts.text2.style.opacity = "100%";

      elts.text1.style.filter = "";
      elts.text1.style.opacity = "0%";
    }

    function animate() {
      requestAnimationFrame(animate);

      let newTime = new Date();
      let shouldIncrementIndex = cooldown > 0;
      let dt = (newTime - time) / 1000;
      time = newTime;

      cooldown -= dt;

      if (cooldown <= 0) {
        if (shouldIncrementIndex) {
          textIndex++;
        }

        doMorph();
      } else {
        doCooldown();
      }
    }

    animate();
    })();
  `;
    this.scriptElement = script;
    this.renderer.appendChild(this.el.nativeElement, script);
  }

  ngOnDestroy() {
    this.removeExistingScript();
  }

  private removeExistingScript() {
    if (this.scriptElement) {
      this.renderer.removeChild(this.el.nativeElement, this.scriptElement);
      console.log('Script removed');
      this.scriptElement = null;
    }
  }
}
