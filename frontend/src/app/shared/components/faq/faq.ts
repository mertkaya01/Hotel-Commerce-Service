import { Component, input } from '@angular/core';
import { MatExpansionModule } from '@angular/material/expansion';

export interface FaqItem {
  question: string;
  answer: string;
}

const DEFAULT_FAQ: FaqItem[] = [
  {
    question: 'Rezervasyonumu nasıl iptal edebilirim?',
    answer:
      'Rezervasyonlarım sayfasından, onaylı rezervasyonunuzun yanındaki "İptal Et" butonuna tıklayarak dilediğiniz zaman iptal edebilirsiniz.',
  },
  {
    question: 'Ödeme nasıl yapılıyor?',
    answer:
      'Bu bir portföy projesidir; gerçek ödeme alınmaz. Rezervasyon akışı, ödeme adımı olmadan uçtan uca çalışır.',
  },
  {
    question: 'Aynı odayı iki kişi aynı tarihe alabilir mi?',
    answer:
      'Hayır. Sistem, seçtiğiniz tarih aralığında oda müsait değilse rezervasyonu engeller; tarih çakışması kontrolü otomatik yapılır.',
  },
  {
    question: 'Otelleri neye göre arayabilirim?',
    answer:
      'Otel adı, şehir veya ülkeye göre arama yapabilir; sonuçları yıldız, şehir ve ülke filtreleriyle daraltabilirsiniz.',
  },
];

@Component({
  selector: 'app-faq',
  imports: [MatExpansionModule],
  template: `
    <mat-accordion class="faq">
      @for (item of items() ?? defaults; track item.question) {
        <mat-expansion-panel>
          <mat-expansion-panel-header>
            <mat-panel-title>{{ item.question }}</mat-panel-title>
          </mat-expansion-panel-header>
          <p>{{ item.answer }}</p>
        </mat-expansion-panel>
      }
    </mat-accordion>
  `,
  styleUrl: './faq.scss',
})
export class Faq {
  readonly items = input<FaqItem[] | null>(null);
  readonly defaults = DEFAULT_FAQ;
}
